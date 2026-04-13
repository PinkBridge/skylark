package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageRequest extends PageRequest {
  /**
   * 用户名（模糊搜索）
   */
  private String username;

  /**
   * 手机号（模糊搜索）
   */
  private String phone;

  /**
   * 邮箱（模糊搜索）
   */
  private String email;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}

