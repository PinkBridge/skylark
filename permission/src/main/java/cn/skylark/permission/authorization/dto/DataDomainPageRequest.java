package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据域分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataDomainPageRequest extends PageRequest {
  /**
   * 数据域名称（模糊搜索）
   */
  private String name;

  /**
   * 数据域编码（模糊搜索）
   */
  private String code;

  /**
   * 数据范围类型
   */
  private String type;

  /**
   * 是否启用
   */
  private Boolean enabled;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}

