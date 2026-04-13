package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统资源实体
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class SysResource {
  /**
   * 资源ID
   */
  private Long id;

  /**
   * 文件名
   */
  private String name;

  /**
   * 原始文件名
   */
  private String originalName;

  /**
   * 文件存储路径
   */
  private String filePath;

  /**
   * 文件类型：IMAGE-图片，DOCUMENT-文档，VIDEO-视频，AUDIO-音频，OTHER-其他
   */
  private String fileType;

  /**
   * 文件大小（字节）
   */
  private Long fileSize;

  /**
   * MIME类型
   */
  private String mimeType;

  /**
   * 访问URL
   */
  private String url;

  /**
   * 描述
   */
  private String description;

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}

