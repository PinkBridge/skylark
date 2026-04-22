package cn.skylark.datadomain.starter.mybatis;

import cn.skylark.datadomain.starter.SkylarkDataDomainProperties;
import cn.skylark.datadomain.starter.SkylarkDataDomainProperties.RowScopeTableRule;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Tenant + row-level data scope rewriting for MyBatis (aligned with permission service behavior).
 */
@Slf4j
@RequiredArgsConstructor
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class DataDomainMybatisInterceptor implements Interceptor {

  private final SkylarkDataDomainProperties props;

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Long tenantId = DataDomainContext.getTenantId();
    if (tenantId == null) {
      return invocation.proceed();
    }
    List<String> tenantTables = props.getTenantTables();
    if (tenantTables == null || tenantTables.isEmpty()) {
      return invocation.proceed();
    }

    MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
    Object parameter = invocation.getArgs()[1];
    BoundSql boundSql = mappedStatement.getBoundSql(parameter);
    String originalSql = boundSql.getSql();

    if (!shouldIntercept(originalSql, tenantTables)) {
      return invocation.proceed();
    }

    if (DataDomainContext.isAllPlatformDataScope()) {
      try {
        Statement st0 = CCJSqlParserUtil.parse(originalSql);
        if (st0 instanceof Select && isPlainSelectSingleListedTableOnly((Select) st0, allPlatformSkipTables())) {
          return invocation.proceed();
        }
      } catch (JSQLParserException ignored) {
        // continue
      }
    }

    try {
      Statement statement = CCJSqlParserUtil.parse(originalSql);
      String modifiedSql = originalSql;

      if (statement instanceof Select) {
        modifiedSql = processSelect((Select) statement, tenantId);
      } else if (statement instanceof Insert) {
        modifiedSql = processInsert((Insert) statement, tenantId, originalSql);
      } else if (statement instanceof Update) {
        modifiedSql = processUpdate((Update) statement, tenantId, originalSql);
      } else if (statement instanceof Delete) {
        modifiedSql = processDelete((Delete) statement, tenantId, originalSql);
      }

      if (!originalSql.equals(modifiedSql)) {
        BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), modifiedSql,
            boundSql.getParameterMappings(), parameter);
        for (org.apache.ibatis.mapping.ParameterMapping mapping : boundSql.getParameterMappings()) {
          String prop = mapping.getProperty();
          if (boundSql.hasAdditionalParameter(prop)) {
            newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
          }
        }
        newBoundSql.setAdditionalParameter("tenantId", tenantId);
        MappedStatement newMappedStatement = copyFromMappedStatement(mappedStatement,
            new BoundSqlSqlSource(newBoundSql));
        invocation.getArgs()[0] = newMappedStatement;
      }
    } catch (JSQLParserException e) {
      log.warn("Failed to parse SQL, use original: {}", originalSql, e);
    }

    return invocation.proceed();
  }

  private List<String> allPlatformSkipTables() {
    List<String> skip = props.getAllPlatformSkipTenantSelectTables();
    if (skip != null && !skip.isEmpty()) {
      return skip;
    }
    return props.getTenantTables();
  }

  private boolean shouldIntercept(String sql, List<String> tenantTables) {
    if (!StringUtils.hasText(sql)) {
      return false;
    }
    String upperSql = sql.toUpperCase(Locale.ROOT).trim();
    if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("INSERT") &&
        !upperSql.startsWith("UPDATE") && !upperSql.startsWith("DELETE")) {
      return false;
    }
    boolean containsTenantTable = false;
    for (String table : tenantTables) {
      if (StringUtils.hasText(table) && sql.contains(table)) {
        containsTenantTable = true;
        break;
      }
    }
    List<String> excludes = props.getExcludeTables();
    if (excludes != null) {
      for (String excludeTable : excludes) {
        if (StringUtils.hasText(excludeTable) && sql.contains(excludeTable)) {
          return false;
        }
      }
    }
    return containsTenantTable;
  }

  private boolean isPlainSelectSingleListedTableOnly(Select select, List<String> listedTables) {
    if (!(select.getSelectBody() instanceof PlainSelect)) {
      return false;
    }
    PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
    if (plainSelect.getJoins() != null && !plainSelect.getJoins().isEmpty()) {
      return false;
    }
    FromItem fromItem = plainSelect.getFromItem();
    if (!(fromItem instanceof Table)) {
      return false;
    }
    String name = ((Table) fromItem).getName();
    if (name == null) {
      return false;
    }
    name = name.replace("`", "").trim();
    return isListedTable(name, listedTables);
  }

  private static boolean isListedTable(String tableName, List<String> listed) {
    if (!StringUtils.hasText(tableName) || listed == null) {
      return false;
    }
    for (String t : listed) {
      if (StringUtils.hasText(t) && t.equalsIgnoreCase(tableName)) {
        return true;
      }
    }
    return false;
  }

  private static String normalizeTableName(String tableName) {
    if (tableName == null) {
      return "";
    }
    return tableName.replace("`", "").trim().toLowerCase(Locale.ROOT);
  }

  private boolean shouldApplyRowDataScope() {
    if (DataDomainContext.isAllPlatformDataScope()) {
      return false;
    }
    if (DataDomainContext.isDataScopeWholeTenant()) {
      return false;
    }
    List<Long> orgIds = DataDomainContext.getDataScopeOrgIds();
    boolean hasOrgs = orgIds != null && !orgIds.isEmpty();
    boolean self = DataDomainContext.isDataScopeSelfOnly();
    return hasOrgs || self;
  }

  private RowScopeTableRule findRule(String normalizedTableName) {
    List<RowScopeTableRule> rules = props.getRowScopeRules();
    if (rules == null) {
      return null;
    }
    for (RowScopeTableRule r : rules) {
      if (r == null || !StringUtils.hasText(r.getTable())) {
        continue;
      }
      if (r.getTable().trim().toLowerCase(Locale.ROOT).equals(normalizedTableName)) {
        return r;
      }
    }
    return null;
  }

  private void appendRowDataScopeWhere(PlainSelect plainSelect, Table table) {
    if (!shouldApplyRowDataScope()) {
      return;
    }
    Expression scopeExpr = buildRowDataScopeExpression(table, normalizeTableName(table.getName()));
    if (scopeExpr == null) {
      return;
    }
    if (plainSelect.getWhere() != null) {
      plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), scopeExpr));
    } else {
      plainSelect.setWhere(scopeExpr);
    }
  }

  private void appendRowDataScopeMutation(Table table, java.util.function.Supplier<Expression> getWhere,
      java.util.function.Consumer<Expression> setWhere) {
    if (!shouldApplyRowDataScope()) {
      return;
    }
    Expression scopeExpr = buildRowDataScopeExpression(table, normalizeTableName(table.getName()));
    if (scopeExpr == null) {
      return;
    }
    Expression w = getWhere.get();
    if (w != null) {
      setWhere.accept(new AndExpression(w, scopeExpr));
    } else {
      setWhere.accept(scopeExpr);
    }
  }

  private Expression buildRowDataScopeExpression(Table table, String normalizedName) {
    RowScopeTableRule rule = findRule(normalizedName);
    if (rule == null) {
      return null;
    }
    List<Long> orgIds = DataDomainContext.getDataScopeOrgIds();
    boolean hasOrgs = orgIds != null && !orgIds.isEmpty();
    boolean selfOnly = DataDomainContext.isDataScopeSelfOnly();
    Long uid = DataDomainContext.getDataScopeUserId();

    List<Expression> parts = new ArrayList<>();
    if (hasOrgs) {
      if (rule.isOrgIdsOnPrimaryKey()) {
        parts.add(newInExpression(table, "id", orgIds));
      } else if (StringUtils.hasText(rule.getOrgIdColumn())) {
        parts.add(newInExpression(table, rule.getOrgIdColumn(), orgIds));
      }
    }
    if (selfOnly && uid != null && StringUtils.hasText(rule.getSelfUserIdColumn())) {
      EqualsTo eq = new EqualsTo();
      eq.setLeftExpression(new Column(table, rule.getSelfUserIdColumn()));
      eq.setRightExpression(new LongValue(uid));
      parts.add(eq);
    }
    if (parts.isEmpty()) {
      return null;
    }
    if (parts.size() == 1) {
      return parts.get(0);
    }
    Expression combined = parts.get(0);
    for (int i = 1; i < parts.size(); i++) {
      combined = new OrExpression(combined, parts.get(i));
    }
    return new Parenthesis(combined);
  }

  private static InExpression newInExpression(Table table, String column, List<Long> ids) {
    List<Expression> exprs = new ArrayList<>(ids.size());
    for (Long id : ids) {
      if (id != null) {
        exprs.add(new LongValue(id));
      }
    }
    if (exprs.isEmpty()) {
      return null;
    }
    return new InExpression(new Column(table, column), new ExpressionList(exprs));
  }

  private String processSelect(Select select, Long tenantId) {
    String tenantCol = props.getTenantIdColumn();
    if (!(select.getSelectBody() instanceof PlainSelect)) {
      return select.toString();
    }
    PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
    FromItem fromItem = plainSelect.getFromItem();
    if (!(fromItem instanceof Table)) {
      return select.toString();
    }
    Table table = (Table) fromItem;
    if (isTenantTable(table.getName())) {
      boolean alreadyHasTenantInWhere = plainSelect.getWhere() != null
          && plainSelect.getWhere().toString().contains(tenantCol);
      if (!alreadyHasTenantInWhere) {
        net.sf.jsqlparser.expression.operators.relational.EqualsTo tenantCondition =
            new net.sf.jsqlparser.expression.operators.relational.EqualsTo();
        tenantCondition.setLeftExpression(new Column(table, tenantCol));
        tenantCondition.setRightExpression(new LongValue(tenantId));
        if (plainSelect.getWhere() != null) {
          plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), tenantCondition));
        } else {
          plainSelect.setWhere(tenantCondition);
        }
      }
    }
    appendRowDataScopeWhere(plainSelect, table);
    return select.toString();
  }

  private String processInsert(Insert insert, Long tenantId, String originalSql) {
    String tenantCol = props.getTenantIdColumn();
    Table table = insert.getTable();
    if (!isTenantTable(table.getName())) {
      return insert.toString();
    }
    List<net.sf.jsqlparser.schema.Column> columns = insert.getColumns();
    boolean hasTenantId = false;
    if (columns != null) {
      for (net.sf.jsqlparser.schema.Column column : columns) {
        if (tenantCol.equalsIgnoreCase(column.getColumnName())) {
          hasTenantId = true;
          break;
        }
      }
    }
    if (hasTenantId) {
      return originalSql;
    }
    if (columns == null) {
      insert.setColumns(columns = new ArrayList<>());
    }
    columns.add(new net.sf.jsqlparser.schema.Column(tenantCol));
    String insertSql = insert.toString();
    if (insertSql.contains(tenantCol) && insertSql.toUpperCase(Locale.ROOT).contains("VALUES")) {
      int valuesIndex = insertSql.toUpperCase(Locale.ROOT).indexOf("VALUES");
      if (valuesIndex > 0) {
        String beforeValues = insertSql.substring(0, valuesIndex).trim();
        String afterValues = insertSql.substring(valuesIndex);
        int firstParen = afterValues.indexOf('(');
        if (firstParen >= 0) {
          int matchingParen = findMatchingParen(afterValues, firstParen);
          if (matchingParen > firstParen) {
            String valuesContent = afterValues.substring(firstParen + 1, matchingParen);
            String trimmed = valuesContent.trim();
            if (!trimmed.isEmpty() && !trimmed.endsWith(",")) {
              trimmed += ", ";
            }
            trimmed += tenantId;
            return beforeValues + " " + afterValues.substring(0, firstParen + 1) +
                trimmed + afterValues.substring(matchingParen);
          }
        }
      }
    }
    return insert.toString();
  }

  private static int findMatchingParen(String str, int startIndex) {
    int depth = 1;
    for (int i = startIndex + 1; i < str.length(); i++) {
      if (str.charAt(i) == '(') {
        depth++;
      } else if (str.charAt(i) == ')') {
        depth--;
        if (depth == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  private String processUpdate(Update update, Long tenantId, String originalSql) {
    String tenantCol = props.getTenantIdColumn();
    Table table = update.getTable();
    if (!isTenantTable(table.getName())) {
      return update.toString();
    }
    if (update.getWhere() != null) {
      String whereStr = update.getWhere().toString();
      if (whereStr.contains(tenantCol)) {
        return originalSql;
      }
    }
    net.sf.jsqlparser.expression.operators.relational.EqualsTo tenantCondition =
        new net.sf.jsqlparser.expression.operators.relational.EqualsTo();
    tenantCondition.setLeftExpression(new Column(table, tenantCol));
    tenantCondition.setRightExpression(new LongValue(tenantId));
    if (update.getWhere() != null) {
      update.setWhere(new AndExpression(update.getWhere(), tenantCondition));
    } else {
      update.setWhere(tenantCondition);
    }
    appendRowDataScopeMutation(table, update::getWhere, update::setWhere);
    return update.toString();
  }

  private String processDelete(Delete delete, Long tenantId, String originalSql) {
    String tenantCol = props.getTenantIdColumn();
    Table table = delete.getTable();
    if (!isTenantTable(table.getName())) {
      return delete.toString();
    }
    if (delete.getWhere() != null) {
      String whereStr = delete.getWhere().toString();
      if (whereStr.contains(tenantCol)) {
        return originalSql;
      }
    }
    net.sf.jsqlparser.expression.operators.relational.EqualsTo tenantCondition =
        new net.sf.jsqlparser.expression.operators.relational.EqualsTo();
    tenantCondition.setLeftExpression(new Column(table, tenantCol));
    tenantCondition.setRightExpression(new LongValue(tenantId));
    if (delete.getWhere() != null) {
      delete.setWhere(new AndExpression(delete.getWhere(), tenantCondition));
    } else {
      delete.setWhere(tenantCondition);
    }
    appendRowDataScopeMutation(table, delete::getWhere, delete::setWhere);
    return delete.toString();
  }

  private boolean isTenantTable(String tableName) {
    if (tableName == null) {
      return false;
    }
    String name = normalizeTableName(tableName);
    List<String> tenantTables = props.getTenantTables();
    if (tenantTables == null) {
      return false;
    }
    for (String tenantTable : tenantTables) {
      if (StringUtils.hasText(tenantTable) && name.equals(tenantTable.trim().toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  private static MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
    MappedStatement.Builder builder = new MappedStatement.Builder(
        ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
    builder.resource(ms.getResource());
    builder.fetchSize(ms.getFetchSize());
    builder.timeout(ms.getTimeout());
    builder.statementType(ms.getStatementType());
    builder.keyGenerator(ms.getKeyGenerator());
    if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
      builder.keyProperty(String.join(",", ms.getKeyProperties()));
    }
    builder.cache(ms.getCache());
    builder.flushCacheRequired(ms.isFlushCacheRequired());
    builder.useCache(ms.isUseCache());
    if (ms.getResultMaps() != null && !ms.getResultMaps().isEmpty()) {
      builder.resultMaps(ms.getResultMaps());
    }
    if (ms.getResultSetType() != null) {
      builder.resultSetType(ms.getResultSetType());
    }
    if (ms.getDatabaseId() != null) {
      builder.databaseId(ms.getDatabaseId());
    }
    builder.resultOrdered(ms.isResultOrdered());
    if (ms.getLang() != null) {
      builder.lang(ms.getLang());
    }
    return builder.build();
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {
    // optional
  }

  private static class BoundSqlSqlSource implements SqlSource {
    private final BoundSql boundSql;

    BoundSqlSqlSource(BoundSql boundSql) {
      this.boundSql = boundSql;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
      return boundSql;
    }
  }
}
