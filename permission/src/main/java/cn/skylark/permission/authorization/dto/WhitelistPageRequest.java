package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 白名单分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WhitelistPageRequest extends PageRequest {
  /**
   * HTTP方法（模糊搜索）
   */
  private String method;

  /**
   * API路径（模糊搜索）
   */
  private String path;

  /**
   * 备注（模糊搜索）
   */
  private String remark;

  /**
   * 是否启用（精确搜索）
   */
  private Boolean enabled;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}

