package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.ChangePasswordDTO;
import cn.skylark.permission.authorization.dto.AdminResetPasswordDTO;
import cn.skylark.permission.authorization.dto.CreateUserDTO;
import cn.skylark.permission.authorization.dto.UpdateMyProfileDTO;
import cn.skylark.permission.authorization.dto.UpdateUserDTO;
import cn.skylark.permission.authorization.dto.UserDTO;
import cn.skylark.permission.authorization.dto.UserPageRequest;
import cn.skylark.permission.authorization.dto.UserResponseDTO;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.dto.ResolvedDataScopeDTO;
import cn.skylark.permission.authorization.service.DataDomainResolutionService;
import cn.skylark.permission.authorization.service.UserService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/permission/users")
public class UserController {

  @Resource
  private UserService userService;
  @Resource
  private DataDomainResolutionService dataDomainResolutionService;

  @GetMapping
  public Ret<List<UserResponseDTO>> list() {
    return Ret.data(userService.listDTO());
  }

  /**
   * 分页查询用户列表（支持搜索）
   *
   * @param page       页码，从1开始，默认1
   * @param size       每页大小，默认10
   * @param username   用户名（模糊搜索）
   * @param phone      手机号（模糊搜索）
   * @param email      邮箱（模糊搜索）
   * @param createTime 创建时间（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<UserResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) LocalDateTime createTime) {
    UserPageRequest pageRequest = new UserPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setUsername(username);
    pageRequest.setPhone(phone);
    pageRequest.setEmail(email);
    pageRequest.setCreateTime(createTime);
    return Ret.data(userService.pageDTOWithCondition(pageRequest));
  }

  /**
   * 获取当前登录用户信息
   */
  @GetMapping("/me")
  public Ret<UserDTO> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Ret.fail(401, "user.not.login");
    }

    String username = null;
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      username = ((UserDetails) principal).getUsername();
    } else if (principal instanceof String) {
      username = (String) principal;
    }

    if (username == null || username.isEmpty()) {
      return Ret.fail(401, "user.info.not.available");
    }

    SysUser user = userService.findByUsername(username);
    if (user == null) {
      return Ret.fail(404, "user.not.found");
    }

    List<SysRole> roles = userService.roles(user.getId());
    UserResponseDTO userDTO = userService.getDTO(user.getId());
    UserDTO dto = new UserDTO();
    dto.setUser(userDTO);
    dto.setRoles(roles);
    ResolvedDataScopeDTO dataScope = dataDomainResolutionService.resolve(user, roles);
    dto.setDataScope(dataScope);
    return Ret.data(dto);
  }

  /**
   * 当前登录用户更新个人资料（头像、手机、邮箱、性别、详细地址）
   */
  @PutMapping("/me")
  public Ret<Integer> updateMyProfile(@RequestBody UpdateMyProfileDTO body) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Ret.fail(401, "user.not.login");
    }
    String username = null;
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      username = ((UserDetails) principal).getUsername();
    } else if (principal instanceof String) {
      username = (String) principal;
    }
    if (username == null || username.isEmpty()) {
      return Ret.fail(401, "user.info.not.available");
    }
    SysUser user = userService.findByUsername(username);
    if (user == null) {
      return Ret.fail(404, "user.not.found");
    }
    if (body != null) {
      if (body.getGender() != null && !StringUtils.hasText(body.getGender())) {
        body.setGender(null);
      } else if (StringUtils.hasText(body.getGender())) {
        String g = body.getGender().trim().toUpperCase();
        if (!"M".equals(g) && !"F".equals(g)) {
          return Ret.fail(400, "user.gender.invalid");
        }
        body.setGender(g);
      }
    }
    return Ret.data(userService.updateMyProfile(user.getId(), body == null ? new UpdateMyProfileDTO() : body));
  }

  @GetMapping("/{id}")
  public Ret<UserDTO> get(@PathVariable Long id) {
    UserResponseDTO userDTO = userService.getDTO(id);
    if (userDTO == null) {
      return Ret.fail(404, "user.not.found");
    }
    List<SysRole> roles = userService.roles(id);
    UserDTO dto = new UserDTO();
    dto.setUser(userDTO);
    dto.setRoles(roles);
    return Ret.data(dto);
  }

  @PostMapping
  public Ret<Long> create(@RequestBody CreateUserDTO dto) {
    Long userId = userService.createWithRoles(dto);
    if (userId == null) {
      return Ret.fail(500, "user.create.failed");
    }
    return Ret.data(userId);
  }

  @PutMapping("/{id}")
  public Ret<Integer> update(@PathVariable Long id, @RequestBody UpdateUserDTO updateUserDTO) {
    UserResponseDTO userDTO = userService.getDTO(id);
    if (userDTO == null) {
      return Ret.fail(404, "user.not.found");
    }
    return Ret.data(userService.updateUserInfo(id, updateUserDTO));
  }

  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    return Ret.data(userService.delete(id));
  }

  @GetMapping("/{id}/roles")
  public Ret<List<SysRole>> userRoles(@PathVariable Long id) {
    return Ret.data(userService.roles(id));
  }

  @PostMapping("/{id}/roles:bind")
  public Ret<Void> bindRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
    userService.bindRoles(id, roleIds);
    return Ret.ok();
  }

  /**
   * 管理员重置指定用户密码（无需旧密码）
   */
  @PutMapping("/{id}/password:reset")
  public Ret<Void> adminResetPassword(@PathVariable Long id, @RequestBody AdminResetPasswordDTO body) {
    if (body == null || !StringUtils.hasText(body.getNewPassword())) {
      return Ret.fail(400, "password.new.required");
    }
    String pw = body.getNewPassword().trim();
    if (pw.length() < 6 || pw.length() > 20) {
      return Ret.fail(400, "password.length.invalid");
    }
    SysUser user = userService.get(id);
    if (user == null) {
      return Ret.fail(404, "user.not.found");
    }
    userService.adminResetPassword(id, pw);
    return Ret.ok();
  }

  /**
   * 当前用户重置密码
   */
  @PutMapping("/me/password")
  public Ret<Void> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
    // 获取当前登录用户
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Ret.fail(401, "user.not.login");
    }

    String username = null;
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      username = ((UserDetails) principal).getUsername();
    } else if (principal instanceof String) {
      username = (String) principal;
    }

    if (username == null || username.isEmpty()) {
      return Ret.fail(401, "user.info.not.available");
    }

    // 参数验证
    if (!StringUtils.hasText(changePasswordDTO.getOldPassword())) {
      return Ret.fail(400, "password.old.required");
    }
    if (!StringUtils.hasText(changePasswordDTO.getNewPassword())) {
      return Ret.fail(400, "password.new.required");
    }
    if (changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())) {
      return Ret.fail(400, "password.same.as.old");
    }

    // 获取用户信息
    SysUser user = userService.findByUsername(username);
    if (user == null) {
      return Ret.fail(404, "user.not.found");
    }

    // 重置密码
    boolean success = userService.changePassword(user.getId(), 
                                                  changePasswordDTO.getOldPassword(), 
                                                  changePasswordDTO.getNewPassword());
    if (!success) {
      return Ret.fail(400, "password.incorrect");
    }

    return Ret.ok();
  }
}

