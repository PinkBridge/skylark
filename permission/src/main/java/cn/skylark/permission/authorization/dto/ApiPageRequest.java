package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * API分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiPageRequest extends PageRequest {
  /**
   * HTTP方法（模糊搜索）
   */
  private String method;

  /**
   * API路径（模糊搜索）
   */
  private String path;

  /**
   * 权限标签（模糊搜索）
   */
  private String permlabel;

  /**
   * 模块键（模糊搜索）
   */
  private String moduleKey;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}

