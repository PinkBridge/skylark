package cn.skylark.permission.authorization.interceptor;

import cn.skylark.permission.authorization.context.TenantContext;
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
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 租户拦截器
 * 自动在SQL中添加租户ID条件
 * - SELECT: 自动添加 WHERE tenant_id = ? 条件
 * - INSERT: 自动添加 tenant_id 字段和值
 * - UPDATE: 自动添加 WHERE tenant_id = ? 条件
 * - DELETE: 自动添加 WHERE tenant_id = ? 条件
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Slf4j
public class TenantInterceptor implements Interceptor {

  /**
   * 需要添加租户ID的表名（白名单）
   * 如果为空，则对所有表生效
   */
  private static final String[] TENANT_TABLES = {
      "sys_user",
      "sys_resource",
      "sys_organization",
      "sys_role",
      "sys_data_domain",
      "sys_login_log",
      "sys_operation_log"
      // "sys_menu",
      // "sys_api"
  };

  /**
   * 租户ID字段名
   */
  private static final String TENANT_ID_COLUMN = "tenant_id";

  /**
   * 不需要添加租户ID的表名（黑名单）
   */
  private static final String[] EXCLUDE_TABLES = {
      "sys_tenant", "sys_whitelist", "oauth_client_details", "oauth_code",
      "oauth_refresh_token", "oauth_approvals", "sys_user_role", "sys_role_api",
      "sys_role_menu", "token_blacklist"
  };

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    // 获取当前租户ID
    Long tenantId = TenantContext.getTenantId();
    if (tenantId == null) {
      // 如果没有租户ID，直接执行原SQL
      log.debug("No tenant ID found, skip tenant interceptor");
      return invocation.proceed();
    }

    MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
    Object parameter = invocation.getArgs()[1];
    BoundSql boundSql = mappedStatement.getBoundSql(parameter);
    String originalSql = boundSql.getSql();

    // 检查是否需要处理
    if (!shouldIntercept(originalSql)) {
      return invocation.proceed();
    }

    // 数据域 ALL：对 TENANT_TABLES 中单表、无 JOIN 的 SELECT 不按当前租户裁剪，与 ResolvedDataScopeDTO.allPlatform 对齐
    if (TenantContext.isAllPlatformDataScope()) {
      try {
        Statement st0 = CCJSqlParserUtil.parse(originalSql);
        if (st0 instanceof Select && isPlainSelectSingleTenantTableOnly((Select) st0)) {
          return invocation.proceed();
        }
      } catch (JSQLParserException ignored) {
        // 解析失败则继续走下方租户条件逻辑
      }
    }

    try {
      // 解析SQL
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
        log.debug("Original SQL: {}", originalSql);
        log.debug("Modified SQL: {}", modifiedSql);

        // 创建新的BoundSql
        BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), modifiedSql,
            boundSql.getParameterMappings(), parameter);
        // 复制参数
        for (org.apache.ibatis.mapping.ParameterMapping mapping : boundSql.getParameterMappings()) {
          String prop = mapping.getProperty();
          if (boundSql.hasAdditionalParameter(prop)) {
            newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
          }
        }
        // 添加租户ID参数
        newBoundSql.setAdditionalParameter("tenantId", tenantId);

        // 创建新的MappedStatement
        MappedStatement newMappedStatement = copyFromMappedStatement(mappedStatement,
            new BoundSqlSqlSource(newBoundSql));

        invocation.getArgs()[0] = newMappedStatement;
      }
    } catch (JSQLParserException e) {
      log.warn("Failed to parse SQL, use original SQL: {}", originalSql, e);
      // 如果解析失败，使用原SQL
    }

    return invocation.proceed();
  }

  /**
   * 是否为「仅主表单表、无 JOIN」且主表在本拦截器 {@link #TENANT_TABLES} 白名单内的 SELECT（列表/分页/count 等），
   * 用于 allPlatform 时跳过租户行条件；含 JOIN 或子查询主表则仍走租户裁剪，避免误放开。
   */
  private static boolean isPlainSelectSingleTenantTableOnly(Select select) {
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
    return isListedTenantTable(name);
  }

  private static boolean isListedTenantTable(String tableName) {
    if (!StringUtils.hasText(tableName)) {
      return false;
    }
    for (String t : TENANT_TABLES) {
      if (t.equalsIgnoreCase(tableName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检查是否需要拦截
   */
  private boolean shouldIntercept(String sql) {
    if (!StringUtils.hasText(sql)) {
      return false;
    }

    String upperSql = sql.toUpperCase().trim();
    // 只处理 SELECT, INSERT, UPDATE, DELETE
    if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("INSERT") &&
        !upperSql.startsWith("UPDATE") && !upperSql.startsWith("DELETE")) {
      return false;
    }

    // 检查是否包含租户表
    boolean containsTenantTable = false;
    for (String table : TENANT_TABLES) {
      if (sql.contains(table)) {
        containsTenantTable = true;
        break;
      }
    }

    // 检查是否在排除列表中
    for (String excludeTable : EXCLUDE_TABLES) {
      if (sql.contains(excludeTable)) {
        return false;
      }
    }

    return containsTenantTable;
  }

  private static String normalizeTableName(String tableName) {
    if (tableName == null) {
      return "";
    }
    return tableName.replace("`", "").trim().toLowerCase();
  }

  private static boolean shouldApplyRowDataScope() {
    if (TenantContext.isAllPlatformDataScope()) {
      return false;
    }
    if (TenantContext.isDataScopeWholeTenant()) {
      return false;
    }
    List<Long> orgIds = TenantContext.getDataScopeOrgIds();
    boolean hasOrgs = orgIds != null && !orgIds.isEmpty();
    boolean self = TenantContext.isDataScopeSelfOnly();
    return hasOrgs || self;
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

  private static Expression buildRowDataScopeExpression(Table table, String normalizedName) {
    if ("sys_user".equals(normalizedName)) {
      return buildSysUserRowScope(table);
    }
    if ("sys_organization".equals(normalizedName)) {
      return buildSysOrganizationRowScope(table);
    }
    return null;
  }

  private static Expression buildSysOrganizationRowScope(Table table) {
    List<Long> orgIds = TenantContext.getDataScopeOrgIds();
    if (orgIds == null || orgIds.isEmpty()) {
      return null;
    }
    return newInExpression(table, "id", orgIds);
  }

  private static Expression buildSysUserRowScope(Table table) {
    List<Long> orgIds = TenantContext.getDataScopeOrgIds();
    boolean hasOrgs = orgIds != null && !orgIds.isEmpty();
    boolean selfOnly = TenantContext.isDataScopeSelfOnly();
    Long uid = TenantContext.getDataScopeUserId();
    List<Expression> parts = new ArrayList<>();
    if (hasOrgs) {
      parts.add(newInExpression(table, "org_id", orgIds));
    }
    if (selfOnly && uid != null) {
      EqualsTo eq = new EqualsTo();
      eq.setLeftExpression(new Column(table, "id"));
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
    return new InExpression(new Column(table, column), new ExpressionList(exprs));
  }

  /**
   * 处理SELECT语句
   */
  private String processSelect(Select select, Long tenantId) {
    if (select.getSelectBody() instanceof PlainSelect) {
      PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
      Table table = (Table) plainSelect.getFromItem();

      if (isTenantTable(table.getName())) {
        boolean alreadyHasTenantInWhere = plainSelect.getWhere() != null
            && plainSelect.getWhere().toString().contains(TENANT_ID_COLUMN);
        if (!alreadyHasTenantInWhere) {
          net.sf.jsqlparser.expression.BinaryExpression tenantCondition =
              new net.sf.jsqlparser.expression.operators.relational.EqualsTo();
          tenantCondition.setLeftExpression(new Column(table, TENANT_ID_COLUMN));
          tenantCondition.setRightExpression(new LongValue(tenantId));

          if (plainSelect.getWhere() != null) {
            plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), tenantCondition));
          } else {
            plainSelect.setWhere(tenantCondition);
          }
        }
      }
      appendRowDataScopeWhere(plainSelect, table);
    }
    return select.toString();
  }

  /**
   * 处理INSERT语句
   *
   * @param originalSql MyBatis 原始 SQL（含 #{}/? 占位符）；若 INSERT 已含 tenant_id 列则必须原样返回，
   *                    避免 JSQLParser 重写后与参数映射不一致，导致写入错误的租户 ID。
   */
  private String processInsert(Insert insert, Long tenantId, String originalSql) {
    Table table = insert.getTable();
    if (isTenantTable(table.getName())) {
      // 检查列中是否已包含tenant_id
      List<Column> columns = insert.getColumns();
      boolean hasTenantId = false;
      if (columns != null) {
        for (Column column : columns) {
          if (TENANT_ID_COLUMN.equalsIgnoreCase(column.getColumnName())) {
            hasTenantId = true;
            break;
          }
        }
      }

      if (hasTenantId) {
        return originalSql;
      }

      if (!hasTenantId) {
        // 添加tenant_id列
        if (columns == null) {
          insert.setColumns(columns = new java.util.ArrayList<>());
        }
        columns.add(new Column(TENANT_ID_COLUMN));

        // 对于INSERT语句，由于JSqlParser API的复杂性，我们使用字符串处理
        // 在实际使用中，建议在Mapper XML中显式指定tenant_id字段
        String insertSql = insert.toString();
        // 如果SQL中已经包含了tenant_id，直接返回
        if (insertSql.contains(TENANT_ID_COLUMN)) {
          return insertSql;
        }
        
        // 尝试在VALUES子句中添加tenant_id值
        // 注意：这种方式可能不够精确，建议在Mapper XML中显式处理
        if (insertSql.toUpperCase().contains("VALUES")) {
          // 在列名列表后添加tenant_id
          int valuesIndex = insertSql.toUpperCase().indexOf("VALUES");
          if (valuesIndex > 0) {
            String beforeValues = insertSql.substring(0, valuesIndex).trim();
            String afterValues = insertSql.substring(valuesIndex);
            
            // 在VALUES后的值列表中添加tenant_id
            // 查找第一个括号对
            int firstParen = afterValues.indexOf('(');
            if (firstParen >= 0) {
              int matchingParen = findMatchingParen(afterValues, firstParen);
              if (matchingParen > firstParen) {
                String valuesContent = afterValues.substring(firstParen + 1, matchingParen);
                // 在值列表末尾添加tenant_id
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
      }
    }
    return insert.toString();
  }

  /**
   * 查找匹配的右括号
   */
  private int findMatchingParen(String str, int startIndex) {
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

  /**
   * 处理UPDATE语句
   */
  private String processUpdate(Update update, Long tenantId, String originalSql) {
    Table table = update.getTable();
    if (isTenantTable(table.getName())) {
      // 检查WHERE子句是否已包含tenant_id
      if (update.getWhere() != null) {
        String whereStr = update.getWhere().toString();
        if (whereStr.contains(TENANT_ID_COLUMN)) {
          return originalSql;
        }
      }

      // 添加tenant_id条件
      net.sf.jsqlparser.expression.BinaryExpression tenantCondition =
          new net.sf.jsqlparser.expression.operators.relational.EqualsTo();
      tenantCondition.setLeftExpression(new Column(table, TENANT_ID_COLUMN));
      tenantCondition.setRightExpression(new LongValue(tenantId));

      if (update.getWhere() != null) {
        update.setWhere(new AndExpression(update.getWhere(), tenantCondition));
      } else {
        update.setWhere(tenantCondition);
      }
      appendRowDataScopeMutation(table, update::getWhere, update::setWhere);
    }
    return update.toString();
  }

  /**
   * 处理DELETE语句
   */
  private String processDelete(Delete delete, Long tenantId, String originalSql) {
    Table table = delete.getTable();
    if (isTenantTable(table.getName())) {
      // 检查WHERE子句是否已包含tenant_id
      if (delete.getWhere() != null) {
        String whereStr = delete.getWhere().toString();
        if (whereStr.contains(TENANT_ID_COLUMN)) {
          return originalSql;
        }
      }

      // 添加tenant_id条件
      net.sf.jsqlparser.expression.BinaryExpression tenantCondition =
          new net.sf.jsqlparser.expression.operators.relational.EqualsTo();
      tenantCondition.setLeftExpression(new Column(table, TENANT_ID_COLUMN));
      tenantCondition.setRightExpression(new LongValue(tenantId));

      if (delete.getWhere() != null) {
        delete.setWhere(new AndExpression(delete.getWhere(), tenantCondition));
      } else {
        delete.setWhere(tenantCondition);
      }
      appendRowDataScopeMutation(table, delete::getWhere, delete::setWhere);
    }
    return delete.toString();
  }

  /**
   * 检查是否是租户表
   */
  private boolean isTenantTable(String tableName) {
    if (tableName == null) {
      return false;
    }
    String name = tableName.toLowerCase();
    for (String tenantTable : TENANT_TABLES) {
      if (name.equals(tenantTable)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 复制MappedStatement
   */
  private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
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
    
    // 保留resultMaps配置
    if (ms.getResultMaps() != null && !ms.getResultMaps().isEmpty()) {
      builder.resultMaps(ms.getResultMaps());
    }
    
    // 保留resultType配置
    // if (ms.getResultType() != null) {
    //   builder.resultType(ms.getResultType());
    // }
    
    // 保留resultSetType配置
    if (ms.getResultSetType() != null) {
      builder.resultSetType(ms.getResultSetType());
    }
    
    // 保留databaseId配置
    if (ms.getDatabaseId() != null) {
      builder.databaseId(ms.getDatabaseId());
    }
    
    // 保留resultOrdered配置
    builder.resultOrdered(ms.isResultOrdered());
    
    // 保留lang配置
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
    // 可以从配置文件中读取属性
  }

  /**
   * 自定义SqlSource
   */
  private static class BoundSqlSqlSource implements SqlSource {
    private final BoundSql boundSql;

    public BoundSqlSqlSource(BoundSql boundSql) {
      this.boundSql = boundSql;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
      return boundSql;
    }
  }
}

