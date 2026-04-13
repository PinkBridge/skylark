package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资源分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourcePageRequest extends PageRequest {

  /**
   * 文件名（模糊搜索）
   */
  private String name;

  /**
   * 原始文件名（模糊搜索）
   */
  private String originalName;

  /**
   * 文件类型（精确搜索）
   */
  private String fileType;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}

