package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.PlatformInitStateDTO;
import cn.skylark.permission.authorization.dto.PlatformInitializeRequestDTO;
import cn.skylark.permission.authorization.dto.UpdateOrganizationDTO;
import cn.skylark.permission.authorization.dto.UpdateTenantDTO;
import cn.skylark.permission.authorization.dto.UpdateUserDTO;
import cn.skylark.permission.authorization.entity.SysOrganization;
import cn.skylark.permission.authorization.entity.SysPlatformInitState;
import cn.skylark.permission.authorization.entity.SysTenant;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.mapper.OrganizationMapper;
import cn.skylark.permission.authorization.mapper.PlatformInitStateMapper;
import cn.skylark.permission.authorization.mapper.TenantMapper;
import cn.skylark.permission.authorization.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class PlatformInitService {
  private static final Long DEFAULT_TENANT_ID = 1L;
  private static final Long DEFAULT_ORG_ID = 1L;
  private static final Long DEFAULT_SUPERADMIN_USER_ID = 1L;

  private static final String STATUS_PENDING = "PENDING";
  private static final String STATUS_COMPLETED = "COMPLETED";

  @Resource
  private PlatformInitStateMapper platformInitStateMapper;
  @Resource
  private TenantMapper tenantMapper;
  @Resource
  private OrganizationMapper organizationMapper;
  @Resource
  private UserMapper userMapper;
  @Resource
  private TenantService tenantService;
  @Resource
  private UserService userService;
  @Resource
  private OrganizationService organizationService;

  public PlatformInitStateDTO state() {
    SysPlatformInitState row = ensureSingletonRow();
    PlatformInitStateDTO dto = new PlatformInitStateDTO();
    dto.setStatus(row == null ? STATUS_PENDING : row.getStatus());
    dto.setFinishedAt(row == null ? null : row.getFinishedAt());
    dto.setInitialized(STATUS_COMPLETED.equalsIgnoreCase(dto.getStatus()));
    return dto;
  }

  @Transactional(rollbackFor = Exception.class)
  public void initialize(PlatformInitializeRequestDTO body) {
    SysPlatformInitState row = ensureSingletonRow();
    if (row != null && STATUS_COMPLETED.equalsIgnoreCase(row.getStatus())) {
      throw new IllegalStateException("platform.already.initialized");
    }

    PlatformInitializeRequestDTO.InitTenantDTO tenant = body == null ? null : body.getTenant();
    PlatformInitializeRequestDTO.InitAdminDTO admin = body == null ? null : body.getAdmin();

    // Step 1: update default tenant (id=1)
    SysTenant existingTenant = tenantMapper.selectById(DEFAULT_TENANT_ID);
    if (existingTenant == null) {
      throw new IllegalStateException("tenant.default.not.found");
    }

    String normalizedDomain = tenantService.normalizeDomainValue(tenant == null ? null : tenant.getDomain());
    if (tenantService.isDomainDuplicated(normalizedDomain, DEFAULT_TENANT_ID)) {
      throw new IllegalArgumentException("tenant.domain.duplicate");
    }

    UpdateTenantDTO updateTenantDTO = new UpdateTenantDTO();
    updateTenantDTO.setName(StringUtils.hasText(tenant == null ? null : tenant.getName()) ? tenant.getName().trim() : existingTenant.getName());
    updateTenantDTO.setSystemName(StringUtils.hasText(tenant == null ? null : tenant.getSystemName()) ? tenant.getSystemName().trim() : existingTenant.getSystemName());
    updateTenantDTO.setDomain(normalizedDomain);
    updateTenantDTO.setLogo(StringUtils.hasText(tenant == null ? null : tenant.getLogo()) ? tenant.getLogo().trim() : existingTenant.getLogo());
    updateTenantDTO.setAddress(StringUtils.hasText(tenant == null ? null : tenant.getAddress()) ? tenant.getAddress().trim() : existingTenant.getAddress());
    updateTenantDTO.setDescription(StringUtils.hasText(tenant == null ? null : tenant.getDescription()) ? tenant.getDescription().trim() : existingTenant.getDescription());
    updateTenantDTO.setContactName(StringUtils.hasText(tenant == null ? null : tenant.getContactName()) ? tenant.getContactName().trim() : existingTenant.getContactName());
    updateTenantDTO.setContactPhone(StringUtils.hasText(tenant == null ? null : tenant.getContactPhone()) ? tenant.getContactPhone().trim() : existingTenant.getContactPhone());
    updateTenantDTO.setContactEmail(StringUtils.hasText(tenant == null ? null : tenant.getContactEmail()) ? tenant.getContactEmail().trim() : existingTenant.getContactEmail());
    // Keep existing status/expireTime to avoid nulling them.
    updateTenantDTO.setStatus(existingTenant.getStatus());
    updateTenantDTO.setExpireTime(existingTenant.getExpireTime());
    tenantService.updateTenantInfo(DEFAULT_TENANT_ID, updateTenantDTO);

    // Step 1.5: keep default organization (id=1) in sync for display purposes
    SysOrganization existingOrg = organizationMapper.selectById(DEFAULT_ORG_ID);
    if (existingOrg != null) {
      UpdateOrganizationDTO updateOrgDTO = new UpdateOrganizationDTO();
      updateOrgDTO.setName(updateTenantDTO.getName());
      updateOrgDTO.setParentId(existingOrg.getParentId());
      updateOrgDTO.setType(existingOrg.getType());
      updateOrgDTO.setLevel(existingOrg.getLevel());
      updateOrgDTO.setSort(existingOrg.getSort());
      updateOrgDTO.setLeader(existingOrg.getLeader());
      updateOrgDTO.setPhone(StringUtils.hasText(updateTenantDTO.getContactPhone()) ? updateTenantDTO.getContactPhone() : existingOrg.getPhone());
      updateOrgDTO.setEmail(StringUtils.hasText(updateTenantDTO.getContactEmail()) ? updateTenantDTO.getContactEmail() : existingOrg.getEmail());
      updateOrgDTO.setAddress(StringUtils.hasText(updateTenantDTO.getAddress()) ? updateTenantDTO.getAddress() : existingOrg.getAddress());
      updateOrgDTO.setDescription(existingOrg.getDescription());
      updateOrgDTO.setStatus(existingOrg.getStatus());
      organizationService.updateOrganizationInfo(DEFAULT_ORG_ID, updateOrgDTO);
    }

    // Step 2: update superadmin account (id=1)
    SysUser existingAdmin = userMapper.selectById(DEFAULT_SUPERADMIN_USER_ID);
    if (existingAdmin == null) {
      throw new IllegalStateException("user.superadmin.not.found");
    }

    // Make sure tenant_id is consistent BEFORE any updates that may be tenant-intercepted.
    // If the current superadmin row has tenant_id NULL (or not 1), tenant interceptor would append
    // "AND tenant_id = 1" to UPDATEs and they would affect 0 rows.
    userService.updateTenantIdByUserId(DEFAULT_SUPERADMIN_USER_ID, DEFAULT_TENANT_ID);

    String newUsername = admin == null ? null : admin.getUsername();
    String newPassword = admin == null ? null : admin.getPassword();
    if (!StringUtils.hasText(newUsername)) {
      throw new IllegalArgumentException("admin.username.required");
    }
    if (!StringUtils.hasText(newPassword)) {
      throw new IllegalArgumentException("admin.password.required");
    }
    newUsername = newUsername.trim();
    if (!newUsername.equals(existingAdmin.getUsername())) {
      SysUser dup = userService.findByUsername(newUsername);
      if (dup != null && dup.getId() != null && !DEFAULT_SUPERADMIN_USER_ID.equals(dup.getId())) {
        throw new IllegalArgumentException("admin.username.duplicate");
      }
    }

    UpdateUserDTO patch = new UpdateUserDTO();
    patch.setUsername(newUsername);
    patch.setEnabled(existingAdmin.getEnabled() != null ? existingAdmin.getEnabled() : Boolean.TRUE);
    patch.setGender(existingAdmin.getGender());
    patch.setAvatar(existingAdmin.getAvatar());
    patch.setPhone(StringUtils.hasText(admin == null ? null : admin.getPhone()) ? admin.getPhone().trim() : existingAdmin.getPhone());
    patch.setEmail(StringUtils.hasText(admin == null ? null : admin.getEmail()) ? admin.getEmail().trim() : existingAdmin.getEmail());
    patch.setStatus(existingAdmin.getStatus());
    patch.setProvince(existingAdmin.getProvince());
    patch.setCity(existingAdmin.getCity());
    patch.setAddress(existingAdmin.getAddress());
    patch.setOrgId(existingAdmin.getOrgId() != null ? existingAdmin.getOrgId() : DEFAULT_ORG_ID);
    userService.updateUserInfo(DEFAULT_SUPERADMIN_USER_ID, patch);
    userMapper.updatePassword(DEFAULT_SUPERADMIN_USER_ID, newPassword);

    // Mark initialized
    platformInitStateMapper.updateStatus(STATUS_COMPLETED, LocalDateTime.now());
  }

  private SysPlatformInitState ensureSingletonRow() {
    SysPlatformInitState row = platformInitStateMapper.selectSingleton();
    if (row != null) {
      return row;
    }
    try {
      platformInitStateMapper.insertSingleton(STATUS_PENDING);
    } catch (Exception ignored) {
      // ignore race
    }
    return platformInitStateMapper.selectSingleton();
  }
}

