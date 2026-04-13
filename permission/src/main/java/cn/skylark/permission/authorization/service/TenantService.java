package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.TenantPageRequest;
import cn.skylark.permission.authorization.dto.TenantResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateTenantDTO;
import cn.skylark.permission.authorization.dto.CreateUserDTO;
import cn.skylark.permission.authorization.entity.OauthClientDetails;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysTenant;
import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.mapper.ApiMapper;
import cn.skylark.permission.authorization.mapper.OauthClientMapper;
import cn.skylark.permission.authorization.mapper.RoleMapper;
import cn.skylark.permission.authorization.mapper.TenantMapper;
import cn.skylark.permission.authorization.mapper.MenuMapper;
import cn.skylark.permission.authorization.mapper.TenantAdminBindingMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 租户服务
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Service
public class TenantService {
  private static final String TENANT_ADMIN_ROLE_NAME = cn.skylark.permission.authorization.support.TenantRoleConstants.TENANT_ADMIN_ROLE_NAME;
  private static final String TENANT_ADMIN_ROLE_REMARK = cn.skylark.permission.authorization.support.TenantRoleConstants.TENANT_ADMIN_ROLE_REMARK;
  private static final List<String> TENANT_ADMIN_MENU_PERMS = cn.skylark.permission.authorization.support.TenantRoleConstants.TENANT_ADMIN_MENU_PERMS;
  private static final List<String> TENANT_ADMIN_API_PERMS = cn.skylark.permission.authorization.support.TenantRoleConstants.TENANT_ADMIN_API_PERMS;

  @Resource
  private TenantMapper tenantMapper;

  @Resource
  private OauthClientMapper oauthClientMapper;

  @Resource
  private RoleMapper roleMapper;

  @Resource
  private MenuMapper menuMapper;

  @Resource
  private ApiMapper apiMapper;

  @Resource
  private UserService userService;

  @Resource
  private MenuService menuService;

  @Resource
  private ApiService apiService;

  @Resource
  private TenantRoleReconcileService tenantRoleReconcileService;

  @Resource
  private TenantAdminBindingMapper tenantAdminBindingMapper;

  @Resource
  private PlatformConfigService platformConfigService;

  public SysTenant get(Long id) {
    return tenantMapper.selectById(id);
  }

  public List<SysTenant> list() {
    return tenantMapper.selectAll();
  }

  public List<SysRole> listRolesByTenantId(Long tenantId) {
    if (tenantId == null) {
      return java.util.Collections.emptyList();
    }
    // Include platform roles (tenant_id=1) so super-admin defined roles can be assigned to tenants.
    java.util.LinkedHashMap<Long, SysRole> dedup = new java.util.LinkedHashMap<>();
    for (SysRole r : roleMapper.selectByTenantId(tenantId)) {
      if (r != null && r.getId() != null) {
        dedup.put(r.getId(), r);
      }
    }
    // Avoid double-adding when tenantId itself is platform tenant (1).
    if (!Long.valueOf(1L).equals(tenantId)) {
      for (SysRole r : roleMapper.selectByTenantId(1L)) {
        if (r != null && r.getId() != null) {
          dedup.putIfAbsent(r.getId(), r);
        }
      }
    }
    return new java.util.ArrayList<>(dedup.values());
  }

  /**
   * 获取租户列表（DTO）
   *
   * @return 租户列表
   */
  public List<TenantResponseDTO> listDTO() {
    List<SysTenant> tenants = tenantMapper.selectAll();
    return tenants.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @Transactional(rollbackFor = Exception.class)
  public int create(SysTenant tenant) {
    if (tenant == null) {
      return 0;
    }
    tenant.setDomain(normalizeDomainValue(tenant.getDomain()));
    tenant.setCode(allocateUniqueTenantCode());
    int rows = tenantMapper.insert(tenant);
    if (rows > 0) {
      syncOauthRedirectUriByTenantDomain(null, tenant.getDomain());
    }
    return rows;
  }

  public int update(SysTenant tenant) {
    return tenantMapper.update(tenant);
  }

  public int delete(Long id) {
    return tenantMapper.deleteById(id);
  }

  /**
   * 获取租户信息（DTO）
   *
   * @param id 租户ID
   * @return 租户信息
   */
  public TenantResponseDTO getDTO(Long id) {
    SysTenant tenant = tenantMapper.selectById(id);
    return tenant != null ? convertToDTO(tenant) : null;
  }

  /**
   * 根据域名获取租户信息（DTO）
   *
   * @param domain 租户域名
   * @return 租户信息
   */
  public TenantResponseDTO getDTOByDomain(String domain) {
    SysTenant tenant = tenantMapper.selectByDomain(domain);
    return tenant != null ? convertToDTO(tenant) : null;
  }

  public TenantResponseDTO getCurrentTenantDTO() {
    Long tenantId = TenantContext.getTenantId();
    if (tenantId == null) {
      return null;
    }
    return getDTO(tenantId);
  }

  /**
   * 分页查询租户列表（DTO）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<TenantResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysTenant> records = tenantMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = tenantMapper.countAll();
    List<TenantResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询租户列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<TenantResponseDTO> pageDTOWithCondition(TenantPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getName()) ||
                           StringUtils.hasText(pageRequest.getCode()) ||
                           StringUtils.hasText(pageRequest.getContactPhone()) ||
                           StringUtils.hasText(pageRequest.getContactEmail()) ||
                           StringUtils.hasText(pageRequest.getDomain()) ||
                           StringUtils.hasText(pageRequest.getStatus()) ||
                           pageRequest.getCreateTime() != null;

    List<SysTenant> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = tenantMapper.selectPageWithCondition(
          pageRequest.getName(),
          pageRequest.getCode(),
          pageRequest.getContactPhone(),
          pageRequest.getContactEmail(),
          pageRequest.getDomain(),
          pageRequest.getStatus(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = tenantMapper.countWithCondition(
          pageRequest.getName(),
          pageRequest.getCode(),
          pageRequest.getContactPhone(),
          pageRequest.getContactEmail(),
          pageRequest.getDomain(),
          pageRequest.getStatus(),
          pageRequest.getCreateTime()
      );
    } else {
      // 使用无条件的查询
      records = tenantMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = tenantMapper.countAll();
    }

    List<TenantResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新租户信息
   *
   * @param tenantId 租户ID
   * @param dto      更新租户信息DTO
   * @return 更新行数
   */
  @Transactional(rollbackFor = Exception.class)
  public int updateTenantInfo(Long tenantId, UpdateTenantDTO dto) {
    if (dto != null) {
      dto.setDomain(normalizeDomainValue(dto.getDomain()));
    }
    SysTenant existing = tenantMapper.selectById(tenantId);
    int rows = tenantMapper.updateTenantInfo(tenantId, dto);
    if (rows > 0) {
      String oldDomain = existing == null ? null : existing.getDomain();
      String newDomain = dto == null ? null : dto.getDomain();
      syncOauthRedirectUriByTenantDomain(oldDomain, newDomain);
    }
    return rows;
  }

  public boolean isDomainDuplicated(String domain, Long excludeTenantId) {
    String normalized = normalizeDomainValue(domain);
    if (!StringUtils.hasText(normalized)) {
      return false;
    }
    SysTenant existed = excludeTenantId == null
        ? tenantMapper.selectByDomain(normalized)
        : tenantMapper.selectByDomainExcludeId(normalized, excludeTenantId);
    return existed != null;
  }

  @Transactional(rollbackFor = Exception.class)
  public int updateCurrentTenantInfo(UpdateTenantDTO dto) {
    Long tenantId = TenantContext.getTenantId();
    if (tenantId == null) {
      throw new IllegalArgumentException("tenant.not.found");
    }
    SysTenant existing = tenantMapper.selectById(tenantId);
    if (existing == null) {
      throw new IllegalArgumentException("tenant.not.found");
    }
    UpdateTenantDTO target = dto == null ? new UpdateTenantDTO() : dto;
    target.setStatus(existing.getStatus());
    target.setExpireTime(existing.getExpireTime());
    if (isDomainDuplicated(target.getDomain(), tenantId)) {
      throw new IllegalArgumentException("tenant.domain.duplicate");
    }
    return updateTenantInfo(tenantId, target);
  }

  @Transactional(rollbackFor = Exception.class)
  public Long createTenantAdminUser(Long tenantId, String username, String password, Long roleId) {
    if (tenantId == null) {
      throw new IllegalArgumentException("tenant.not.found");
    }
    if (!StringUtils.hasText(username)) {
      throw new IllegalArgumentException("tenant.admin.username.required");
    }
    if (!StringUtils.hasText(password)) {
      throw new IllegalArgumentException("tenant.admin.password.required");
    }
    if (userService.findByUsername(username) != null) {
      throw new IllegalArgumentException("tenant.admin.username.duplicate");
    }

    if (roleId == null) {
      throw new IllegalArgumentException("tenant.admin.role.required");
    }
    SysRole role = roleMapper.selectById(roleId);
    if (role == null) {
      throw new IllegalArgumentException("role.not.found");
    }
    // Allow tenant-local roles (tenant_id == tenantId) and platform roles (tenant_id == 1 or NULL).
    if (role.getTenantId() != null && !tenantId.equals(role.getTenantId()) && !Long.valueOf(1L).equals(role.getTenantId())) {
      throw new IllegalArgumentException("role.tenant.mismatch");
    }

    CreateUserDTO createUserDTO = new CreateUserDTO();
    createUserDTO.setUsername(username.trim());
    createUserDTO.setPassword(password);
    createUserDTO.setEnabled(true);
    createUserDTO.setStatus("ACTIVE");
    createUserDTO.setTenantId(tenantId);
    createUserDTO.setRoleIds(Arrays.asList(roleId));
    Long userId = userService.createWithRoles(createUserDTO);
    if (userId != null) {
      // 确保写入目标租户：请求头 X-Tenant-Id 可能与被操作租户不一致，且 INSERT 拦截器曾可能污染 tenant_id
      userService.updateTenantIdByUserId(userId, tenantId);
      tenantAdminBindingMapper.upsert(tenantId, userId, roleId);
    }
    return userId;
  }

  private Long ensureTenantAdminRole(Long tenantId) {
    SysRole role = roleMapper.selectByNameAndTenantId(TENANT_ADMIN_ROLE_NAME, tenantId);
    if (role == null) {
      SysRole newRole = new SysRole();
      newRole.setName(TENANT_ADMIN_ROLE_NAME);
      newRole.setRemark(TENANT_ADMIN_ROLE_REMARK);
      newRole.setTenantId(tenantId);
      roleMapper.insert(newRole);
      role = newRole;
    }
    if (role == null || role.getId() == null) {
      throw new IllegalStateException("tenant.admin.role.create.failed");
    }
    syncTenantAdminRolePermissions(role.getId());
    // 上限（租户管理员权限）发生变化后，自动回收租户下子角色的越界权限
    tenantRoleReconcileService.reconcileTenant(tenantId);
    return role.getId();
  }

  private void syncTenantAdminRolePermissions(Long roleId) {
    List<Long> menuIds = menuMapper.selectIdsByPermlabels(TENANT_ADMIN_MENU_PERMS);
    if (!CollectionUtils.isEmpty(menuIds)) {
      menuService.bindRoleMenus(roleId, menuIds);
    }
    List<Long> apiIds = apiMapper.selectIdsByPermlabels(TENANT_ADMIN_API_PERMS);
    if (!CollectionUtils.isEmpty(apiIds)) {
      apiService.bindRoleApis(roleId, apiIds);
    }
  }

  private void syncOauthRedirectUriByTenantDomain(String oldDomainRaw, String newDomainRaw) {
    String oldHost = normalizeHost(oldDomainRaw);
    String newHost = normalizeHost(newDomainRaw);
    if (!StringUtils.hasText(oldHost) && !StringUtils.hasText(newHost)) {
      return;
    }
    List<String> clientIds = platformConfigService == null
        ? java.util.Collections.emptyList()
        : platformConfigService.tenantDomainSyncClientIds();
    for (String clientId : clientIds) {
      OauthClientDetails client = oauthClientMapper.selectByClientId(clientId);
      if (client == null) {
        continue;
      }
      String updatedRedirectUris = rewriteRedirectUris(client.getWebServerRedirectUri(), oldHost, newHost);
      oauthClientMapper.updateRedirectUriByClientId(clientId, updatedRedirectUris);
    }
  }

  private String rewriteRedirectUris(String redirectUrisRaw, String oldHost, String newHost) {
    Set<String> uris = new LinkedHashSet<>();
    if (StringUtils.hasText(redirectUrisRaw)) {
      String[] parts = redirectUrisRaw.split(",");
      for (String part : parts) {
        if (!StringUtils.hasText(part)) {
          continue;
        }
        String uri = part.trim();
        if (StringUtils.hasText(oldHost) && isHostHomeUri(uri, oldHost)) {
          continue;
        }
        uris.add(uri);
      }
    }
    if (StringUtils.hasText(newHost)) {
      uris.add("http://" + newHost + "/home");
    }
    return String.join(",", new ArrayList<>(uris));
  }

  private boolean isHostHomeUri(String uriRaw, String host) {
    if (!StringUtils.hasText(uriRaw) || !StringUtils.hasText(host)) {
      return false;
    }
    try {
      URI uri = URI.create(uriRaw.trim());
      String uriHost = uri.getHost();
      int uriPort = uri.getPort();
      String targetHost = uriPort > 0 ? (uriHost + ":" + uriPort) : uriHost;
      if (!StringUtils.hasText(targetHost) || !host.equalsIgnoreCase(targetHost)) {
        return false;
      }
      String path = uri.getPath();
      return "/home".equals(path) || "/home/".equals(path);
    } catch (Exception e) {
      return false;
    }
  }

  private String normalizeHost(String rawDomain) {
    if (!StringUtils.hasText(rawDomain)) {
      return "";
    }
    String domain = rawDomain.trim();
    try {
      URI uri = URI.create(domain.contains("://") ? domain : ("http://" + domain));
      String host = uri.getHost();
      if (!StringUtils.hasText(host)) {
        return "";
      }
      int port = uri.getPort();
      return port > 0 ? host + ":" + port : host;
    } catch (Exception e) {
      return "";
    }
  }

  public String normalizeDomainValue(String rawDomain) {
    if (!StringUtils.hasText(rawDomain)) {
      return null;
    }
    String value = rawDomain.trim();
    return StringUtils.hasText(value) ? value : null;
  }

  /**
   * 生成全局唯一的租户编码（客户端不可指定，创建时由服务端写入）。
   */
  private String allocateUniqueTenantCode() {
    for (int attempt = 0; attempt < 32; attempt++) {
      String code = "t" + UUID.randomUUID().toString().replace("-", "");
      if (code.length() > 50) {
        code = code.substring(0, 50);
      }
      if (tenantMapper.selectByCode(code) == null) {
        return code;
      }
    }
    throw new IllegalStateException("tenant.code.allocate.failed");
  }

  /**
   * 将SysTenant转换为TenantResponseDTO
   *
   * @param tenant 租户实体
   * @return 租户响应DTO
   */
  private TenantResponseDTO convertToDTO(SysTenant tenant) {
    if (tenant == null) {
      return null;
    }
    TenantResponseDTO dto = new TenantResponseDTO();
    BeanUtils.copyProperties(tenant, dto);
    return dto;
  }
}


