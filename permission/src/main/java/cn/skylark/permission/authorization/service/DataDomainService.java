package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.DataDomainPageRequest;
import cn.skylark.permission.authorization.dto.DataDomainResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateDataDomainDTO;
import cn.skylark.permission.authorization.entity.SysDataDomain;
import cn.skylark.permission.authorization.mapper.DataDomainMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据域服务
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Service
public class DataDomainService {

  private static final String TYPE_ALL = "ALL";
  private static final String TYPE_TENANT = "TENANT";
  private static final String TYPE_ORG_ALL = "ORG_ALL";
  private static final String TYPE_ORG_AND_CHILD = "ORG_AND_CHILD";
  private static final String TYPE_ORG_ONLY = "ORG_ONLY";
  private static final String TYPE_SELF = "SELF";
  private static final String TYPE_CUSTOM = "CUSTOM";

  @Resource
  private DataDomainMapper dataDomainMapper;

  @Resource
  private TenantPermissionCeilingService tenantPermissionCeilingService;

  /**
   * 根据ID查询数据域
   *
   * @param id 数据域ID
   * @return 数据域
   */
  public SysDataDomain get(Long id) {
    return dataDomainMapper.selectById(id);
  }

  /**
   * 查询所有数据域
   *
   * @return 数据域列表
   */
  public List<SysDataDomain> list() {
    return dataDomainMapper.selectAll();
  }

  /**
   * 插入数据域
   *
   * @param dataDomain 数据域
   * @return 插入行数
   */
  public int create(SysDataDomain dataDomain) {
    if (dataDomain.getTenantId() == null) {
      dataDomain.setTenantId(TenantContext.getTenantId());
    }
    validateDefinition(dataDomain.getType(), dataDomain.getScopeValue(), dataDomain.getCustomSql());
    return dataDomainMapper.insert(dataDomain);
  }

  /**
   * 更新数据域
   *
   * @param dataDomain 数据域
   * @return 更新行数
   */
  public int update(SysDataDomain dataDomain) {
    return dataDomainMapper.update(dataDomain);
  }

  /**
   * 删除数据域
   *
   * @param id 数据域ID
   * @return 删除行数
   */
  public int delete(Long id) {
    return dataDomainMapper.deleteById(id);
  }

  /**
   * 获取数据域列表（DTO）
   *
   * @return 数据域列表
   */
  public List<DataDomainResponseDTO> listDTO() {
    List<SysDataDomain> dataDomains = dataDomainMapper.selectAll();
    return dataDomains.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  /**
   * 获取数据域信息（DTO）
   *
   * @param id 数据域ID
   * @return 数据域信息
   */
  public DataDomainResponseDTO getDTO(Long id) {
    SysDataDomain dataDomain = dataDomainMapper.selectById(id);
    return dataDomain != null ? convertToDTO(dataDomain) : null;
  }

  /**
   * 分页查询数据域列表（DTO）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<DataDomainResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysDataDomain> records = dataDomainMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = dataDomainMapper.countAll();
    List<DataDomainResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询数据域列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<DataDomainResponseDTO> pageDTOWithCondition(DataDomainPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getName()) ||
                           StringUtils.hasText(pageRequest.getCode()) ||
                           StringUtils.hasText(pageRequest.getType()) ||
                           pageRequest.getEnabled() != null ||
                           pageRequest.getCreateTime() != null;

    List<SysDataDomain> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = dataDomainMapper.selectPageWithCondition(
          pageRequest.getName(),
          pageRequest.getCode(),
          pageRequest.getType(),
          pageRequest.getEnabled(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = dataDomainMapper.countWithCondition(
          pageRequest.getName(),
          pageRequest.getCode(),
          pageRequest.getType(),
          pageRequest.getEnabled(),
          pageRequest.getCreateTime()
      );
    } else {
      // 使用无条件的查询
      records = dataDomainMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = dataDomainMapper.countAll();
    }

    List<DataDomainResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新数据域信息
   *
   * @param dataDomainId 数据域ID
   * @param dto          更新数据域信息DTO
   * @return 更新行数
   */
  public int updateDataDomainInfo(Long dataDomainId, UpdateDataDomainDTO dto) {
    SysDataDomain cur = dataDomainMapper.selectById(dataDomainId);
    if (cur == null) {
      return 0;
    }
    String type = dto.getType() != null ? dto.getType() : cur.getType();
    String scope = dto.getScopeValue() != null ? dto.getScopeValue() : cur.getScopeValue();
    String custom = dto.getCustomSql() != null ? dto.getCustomSql() : cur.getCustomSql();
    validateDefinition(type, scope, custom);
    return dataDomainMapper.updateDataDomainInfo(dataDomainId, dto);
  }

  /**
   * 租户侧可绑定到角色的数据域列表：
   * <ul>
   *   <li>平台上下文：全量；</li>
   *   <li>正在为「租户管理员」角色绑定时：当前租户下全部数据域（否则管理员从未绑过域时 grantable 恒为空，无法绑第一条）；</li>
   *   <li>其它角色：仅「租户管理员」已绑定的数据域（天花板）。</li>
   * </ul>
   *
   * @param forRoleId 当前正在授权的目标角色 id；不传则按「非管理员」语义只返回天花板集合
   */
  public List<DataDomainResponseDTO> listGrantableDTOs(Long forRoleId) {
    Long tenantId = tenantPermissionCeilingService.resolveTenantIdOrNull();
    if (tenantId == null) {
      return listDTO();
    }
    Long adminRoleId = tenantPermissionCeilingService.tenantAdminRoleId(tenantId);
    if (adminRoleId == null) {
      return Collections.emptyList();
    }
    if (forRoleId != null && forRoleId.equals(adminRoleId)) {
      return listDTO();
    }
    return dataDomainMapper.selectByRoleId(adminRoleId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  /**
   * 校验 type / scopeValue / customSql；当前版本禁止非空的 customSql。
   */
  public void validateDefinition(String type, String scopeValue, String customSql) {
    if (!StringUtils.hasText(type)) {
      throw new IllegalArgumentException("data.domain.type.required");
    }
    String t = type.trim().toUpperCase();
    if (!TYPE_ALL.equals(t) && !TYPE_TENANT.equals(t) && !TYPE_ORG_ALL.equals(t) && !TYPE_ORG_AND_CHILD.equals(t)
        && !TYPE_ORG_ONLY.equals(t) && !TYPE_SELF.equals(t) && !TYPE_CUSTOM.equals(t)) {
      throw new IllegalArgumentException("data.domain.type.invalid");
    }
    if (StringUtils.hasText(customSql)) {
      throw new IllegalArgumentException("data.domain.customSql.not.supported");
    }
    if (TYPE_CUSTOM.equals(t) && StringUtils.hasText(scopeValue)) {
      parseScopeOrgIdsOrThrow(scopeValue.trim());
    }
  }

  private static void parseScopeOrgIdsOrThrow(String jsonArray) {
    try {
      JSONArray arr = JSON.parseArray(jsonArray);
      for (int i = 0; i < arr.size(); i++) {
        arr.getLong(i);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("data.domain.scopeValue.invalid");
    }
  }

  /**
   * 根据角色ID查询数据域列表
   *
   * @param roleId 角色ID
   * @return 数据域列表
   */
  public List<SysDataDomain> listByRole(Long roleId) {
    return dataDomainMapper.selectByRoleId(roleId);
  }

  /**
   * 绑定角色和数据域
   *
   * @param roleId        角色ID
   * @param dataDomainIds 数据域ID列表
   */
  public void bindRoleDataDomains(Long roleId, List<Long> dataDomainIds) {
    dataDomainMapper.deleteBindingsByRoleId(roleId);
    if (dataDomainIds != null && !dataDomainIds.isEmpty()) {
      dataDomainMapper.bindRoleDataDomains(roleId, dataDomainIds);
    }
  }

  /**
   * 将SysDataDomain转换为DataDomainResponseDTO
   *
   * @param dataDomain 数据域实体
   * @return 数据域响应DTO
   */
  private DataDomainResponseDTO convertToDTO(SysDataDomain dataDomain) {
    if (dataDomain == null) {
      return null;
    }
    DataDomainResponseDTO dto = new DataDomainResponseDTO();
    BeanUtils.copyProperties(dataDomain, dto);
    return dto;
  }
}

