package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.CreateUserDTO;
import cn.skylark.permission.authorization.dto.OrganizationResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateMyProfileDTO;
import cn.skylark.permission.authorization.dto.UpdateUserDTO;
import cn.skylark.permission.authorization.dto.UserPageRequest;
import cn.skylark.permission.authorization.dto.UserResponseDTO;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysTenant;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.mapper.TenantMapper;
import cn.skylark.permission.authorization.mapper.UserMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

  @Resource
  private UserMapper userMapper;

  @Resource
  private OrganizationService organizationService;

  @Resource
  private TenantMapper tenantMapper;

  public SysUser get(Long id) {
    return userMapper.selectById(id);
  }

  public List<SysUser> list() {
    return userMapper.selectAll();
  }

  /**
   * 获取用户列表（DTO，不包含密码）
   *
   * @return 用户列表
   */
  public List<UserResponseDTO> listDTO() {
    List<SysUser> users = userMapper.selectAll();
    Map<Long, String> tenantNames = buildTenantNameMap();
    return users.stream().map(u -> convertToDTO(u, tenantNames)).collect(Collectors.toList());
  }

  public int create(SysUser user) {
    return userMapper.insert(user);
  }

  /**
   * 创建用户（包含角色关联）
   *
   * @param dto 创建用户DTO
   * @return 创建的用户ID
   */
  /**
   * 将已存在用户的租户修正为指定值（平台为某租户创建管理员等场景）。
   */
  public void updateTenantIdByUserId(Long userId, Long tenantId) {
    if (userId == null || tenantId == null) {
      return;
    }
    userMapper.updateTenantIdByUserId(userId, tenantId);
  }

  public Long createWithRoles(CreateUserDTO dto) {
    // 创建用户实体
    SysUser user = new SysUser();
    user.setUsername(dto.getUsername());
    user.setPassword(dto.getPassword());
    user.setEnabled(dto.getEnabled());
    user.setGender(dto.getGender());
    user.setAvatar(dto.getAvatar());
    user.setPhone(dto.getPhone());
    user.setEmail(dto.getEmail());
    user.setStatus(dto.getStatus());
    user.setProvince(dto.getProvince());
    user.setCity(dto.getCity());
    user.setAddress(dto.getAddress());
    user.setOrgId(dto.getOrgId());
    user.setTenantId(dto.getTenantId());
    
    // 自动设置租户ID（如果未提供）
    if (user.getTenantId() == null) {
      user.setTenantId(TenantContext.getTenantId());
    }
    
    // 插入用户
    int result = userMapper.insert(user);
    if (result > 0 && user.getId() != null) {
      // 如果提供了角色ID数组，绑定角色
      if (!CollectionUtils.isEmpty(dto.getRoleIds())) {
        bindRoles(user.getId(), dto.getRoleIds());
      }
      return user.getId();
    }
    return null;
  }

  public int update(SysUser user) {
    return userMapper.update(user);
  }

  public int delete(Long id) {
    userMapper.deleteUserRoles(id);
    return userMapper.deleteById(id);
  }

  public List<SysRole> roles(Long userId) {
    return userMapper.selectRolesByUserId(userId);
  }

  public void bindRoles(Long userId, List<Long> roleIds) {
    userMapper.deleteUserRoles(userId);
    if (!CollectionUtils.isEmpty(roleIds)) {
      userMapper.insertUserRoles(userId, roleIds);
    }
  }

  public SysUser findByUsername(String username) {
    return userMapper.findByUsername(username);
  }

  /**
   * 重置密码
   *
   * @param userId      用户ID
   * @param oldPassword 旧密码
   * @param newPassword 新密码
   * @return true-成功，false-失败
   */
  public boolean changePassword(Long userId, String oldPassword, String newPassword) {
    SysUser user = userMapper.selectById(userId);
    if (user == null) {
      return false;
    }

    // 验证旧密码（当前使用NoOpPasswordEncoder，密码是明文存储）
    if (oldPassword == null || !oldPassword.equals(user.getPassword())) {
      return false;
    }

    // 更新密码
    int result = userMapper.updatePassword(userId, newPassword);
    return result > 0;
  }

  /**
   * 分页查询用户列表
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<SysUser> page(PageRequest pageRequest) {
    List<SysUser> records = userMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = userMapper.countAll();
    return new PageResult<>(records, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询用户列表（DTO，不包含密码）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<UserResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysUser> records = userMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = userMapper.countAll();
    Map<Long, String> tenantNames = buildTenantNameMap();
    List<UserResponseDTO> dtoList = records.stream().map(u -> convertToDTO(u, tenantNames)).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询用户列表（带条件，DTO，不包含密码）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<UserResponseDTO> pageDTOWithCondition(UserPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getUsername()) ||
                           StringUtils.hasText(pageRequest.getPhone()) ||
                           StringUtils.hasText(pageRequest.getEmail()) ||
                           pageRequest.getCreateTime() != null;

    List<SysUser> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = userMapper.selectPageWithCondition(
          pageRequest.getUsername(),
          pageRequest.getPhone(),
          pageRequest.getEmail(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = userMapper.countWithCondition(
          pageRequest.getUsername(),
          pageRequest.getPhone(),
          pageRequest.getEmail(),
          pageRequest.getCreateTime()
      );
    } else {
      // 使用无条件的查询
      records = userMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = userMapper.countAll();
    }

    Map<Long, String> tenantNames = buildTenantNameMap();
    List<UserResponseDTO> dtoList = records.stream().map(u -> convertToDTO(u, tenantNames)).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 获取用户信息（DTO，不包含密码）
   *
   * @param id 用户ID
   * @return 用户信息
   */
  public UserResponseDTO getDTO(Long id) {
    SysUser user = userMapper.selectById(id);
    return user != null ? convertToDTO(user, buildTenantNameMap()) : null;
  }

  /**
   * 更新用户信息（不包含密码）
   *
   * @param userId 用户ID
   * @param dto    更新用户信息DTO
   * @return 更新行数
   */
  public int updateUserInfo(Long userId, UpdateUserDTO dto) {
    int result = userMapper.updateUserInfo(userId, dto);
    // 如果提供了角色ID数组，更新角色关联（先删除原有关联，再新增关联）
    if (result > 0 && !CollectionUtils.isEmpty(dto.getRoleIds())) {
      bindRoles(userId, dto.getRoleIds());
    }
    return result;
  }

  /**
   * 当前用户更新个人资料：仅覆盖传入的非 null 字段，其余保持库中原值。
   */
  public int updateMyProfile(Long userId, UpdateMyProfileDTO patch) {
    SysUser current = userMapper.selectById(userId);
    if (current == null) {
      throw new IllegalArgumentException("user.not.found");
    }
    UpdateUserDTO merged = new UpdateUserDTO();
    merged.setUsername(current.getUsername());
    merged.setEnabled(current.getEnabled());
    merged.setGender(patch.getGender() != null ? patch.getGender() : current.getGender());
    merged.setAvatar(patch.getAvatar() != null ? patch.getAvatar() : current.getAvatar());
    merged.setPhone(patch.getPhone() != null ? patch.getPhone() : current.getPhone());
    merged.setEmail(patch.getEmail() != null ? patch.getEmail() : current.getEmail());
    merged.setStatus(current.getStatus());
    merged.setProvince(current.getProvince());
    merged.setCity(current.getCity());
    merged.setAddress(patch.getAddress() != null ? patch.getAddress() : current.getAddress());
    merged.setOrgId(current.getOrgId());
    return userMapper.updateUserInfo(userId, merged);
  }

  /**
   * 将SysUser转换为UserResponseDTO（不包含密码）
   *
   * @param user 用户实体
   * @return 用户响应DTO
   */
  private static Map<Long, String> buildTenantNameMapFromList(List<SysTenant> tenants) {
    Map<Long, String> map = new HashMap<>();
    if (tenants == null) {
      return map;
    }
    for (SysTenant t : tenants) {
      if (t != null && t.getId() != null) {
        map.put(t.getId(), t.getName());
      }
    }
    return map;
  }

  private Map<Long, String> buildTenantNameMap() {
    return buildTenantNameMapFromList(tenantMapper.selectAll());
  }

  private UserResponseDTO convertToDTO(SysUser user, Map<Long, String> tenantNames) {
    if (user == null) {
      return null;
    }
    UserResponseDTO dto = new UserResponseDTO();
    BeanUtils.copyProperties(user, dto);
    if (user.getTenantId() != null && tenantNames != null) {
      dto.setTenantName(tenantNames.get(user.getTenantId()));
    }
    // 如果用户有组织ID，查询并填充组织信息
    if (user.getOrgId() != null) {
      OrganizationResponseDTO organization = organizationService.getDTO(user.getOrgId());
      dto.setOrganization(organization);
    }
    return dto;
  }
}

