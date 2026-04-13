package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class MenuTreeNode {
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
   * 菜单名称
   */
  private String name;

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

  /**
   * 子菜单列表
   */
  private List<MenuTreeNode> children = new ArrayList<>();
}

