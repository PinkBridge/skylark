package cn.skylark.permission.authentication.service;

import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom UserDetailsService, load user information from database
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Service
@Slf4j
public class CustomUserDetailsServiceImpl implements UserDetailsService {

  @Resource
  private UserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("loadUserByUsername:{}", username);
    // get user
    SysUser sysUser = userService.findByUsername(username);
    if (sysUser == null) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }

    // get roles
    List<SysRole> roles = userService.roles(sysUser.getId());
    List<GrantedAuthority> authorities = new ArrayList<>(16);
    for (SysRole role : roles) {
      authorities.add(new SimpleGrantedAuthority(role.getName()));
    }

    // return user
    return User.builder()
            .username(sysUser.getUsername())
            .password(sysUser.getPassword())
            .roles()
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!sysUser.getEnabled())
            .build();
  }
}
