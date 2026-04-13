package cn.skylark.permission.utils;

import lombok.Data;

/**
 * 分页请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class PageRequest {
  /**
   * 页码，从1开始
   */
  private Integer page = 1;

  /**
   * 每页大小
   */
  private Integer size = 10;

  /**
   * 获取偏移量（用于SQL LIMIT）
   */
  public Integer getOffset() {
    if (page == null || page < 1) {
      page = 1;
    }
    if (size == null || size < 1) {
      size = 10;
    }
    return (page - 1) * size;
  }

  /**
   * 获取限制数量（用于SQL LIMIT）
   */
  public Integer getLimit() {
    if (size == null || size < 1) {
      size = 10;
    }
    return size;
  }
}

