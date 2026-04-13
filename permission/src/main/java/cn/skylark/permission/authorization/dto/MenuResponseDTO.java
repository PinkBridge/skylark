package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单响应DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class MenuResponseDTO {
  /**
   * 菜单ID
   */
  private Long id;

  /**
   * 父菜单ID
   */
  private Long parentId;

  /**
   * 父菜单信息
   */
  private ParentMenuDTO parentMenu;

  /**
   * 菜单名称（库表主名称，一般为中文）
   */
  private String name;

  /**
   * 按请求语言解析后的展示名称
   */
  private String displayName;

  /**
   * 多语言名称 JSON（管理端编辑用）
   */
  private String nameI18n;

  /**
   * 菜单路径
   */
  private String path;

  /**
   * 菜单图标
   */
  private String icon;

  /**
   * 排序
   */
  private Integer sort;

  /**
   * 是否隐藏：true-隐藏，false-显示
   */
  private Boolean hidden;

  /**
   * 类型：菜单、按钮
   */
  private String type;

  /**
   * 权限标签
   */
  private String permlabel;

  /**
   * 模块标识
   */
  private String moduleKey;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}

