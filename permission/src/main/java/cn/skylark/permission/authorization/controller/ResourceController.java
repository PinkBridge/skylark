package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.ResourcePageRequest;
import cn.skylark.permission.authorization.dto.ResourceResponseDTO;
import cn.skylark.permission.authorization.entity.SysResource;
import cn.skylark.permission.authorization.service.ResourceService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 资源控制器
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@RestController
@RequestMapping("/api/permission/resources")
@Slf4j
public class ResourceController {

  @Resource
  private ResourceService resourceService;

  /**
   * 文件存储根目录
   */
  @Value("${file.upload.path:./uploads}")
  private String uploadPath;

  /**
   * 文件访问URL前缀
   */
  @Value("${file.upload.url-prefix:/api/permission/resources/download/}")
  private String urlPrefix;

  /**
   * 获取资源列表
   */
  @GetMapping
  public Ret<List<ResourceResponseDTO>> list() {
    return Ret.data(resourceService.listDTO());
  }

  /**
   * 分页查询资源列表（支持搜索）
   *
   * @param page         页码，从1开始，默认1
   * @param size         每页大小，默认10
   * @param name         文件名（模糊搜索）
   * @param originalName 原始文件名（模糊搜索）
   * @param fileType     文件类型（精确搜索）
   * @param createTime   创建时间（查询此时间之前的数据）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<ResourceResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String originalName,
      @RequestParam(required = false) String fileType,
      @RequestParam(required = false) LocalDateTime createTime) {
    ResourcePageRequest pageRequest = new ResourcePageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setName(name);
    pageRequest.setOriginalName(originalName);
    pageRequest.setFileType(fileType);
    pageRequest.setCreateTime(createTime);
    return Ret.data(resourceService.pageDTOWithCondition(pageRequest));
  }

  /**
   * 根据ID获取资源信息
   */
  @GetMapping("/{id}")
  public Ret<ResourceResponseDTO> get(@PathVariable Long id) {
    ResourceResponseDTO resourceDTO = resourceService.getDTO(id);
    if (resourceDTO == null) {
      return Ret.fail(404, "resource.not.found");
    }
    return Ret.data(resourceDTO);
  }

  /**
   * 文件上传
   *
   * @param file        上传的文件
   * @param name        文件名（可选，默认使用原始文件名）
   * @param description 描述（可选）
   * @return 资源信息
   */
  @PostMapping("/upload")
  public Ret<ResourceResponseDTO> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String description) {
    if (file.isEmpty()) {
      return Ret.fail(400, "file.is.empty");
    }

    try {
      // 获取原始文件名
      String originalFilename = file.getOriginalFilename();
      if (originalFilename == null || originalFilename.isEmpty()) {
        return Ret.fail(400, "file.name.invalid");
      }

      // 生成唯一文件名
      String fileExtension = getFileExtension(originalFilename);
      String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

      // 创建存储目录（按日期和租户ID组织）
      Long tenantId = TenantContext.getTenantId();
      String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      String tenantDir = tenantId != null ? "tenant_" + tenantId : "default";
      String relativePath = tenantDir + "/" + dateDir + "/" + uniqueFileName;
      Path filePath = Paths.get(uploadPath, relativePath);
      Files.createDirectories(filePath.getParent());

      // 保存文件
      file.transferTo(filePath.toFile());

      // 确定文件类型
      String fileType = determineFileType(file.getContentType(), fileExtension);

      // 创建资源记录
      SysResource resource = new SysResource();
      resource.setName(StringUtils.hasText(name) ? name : originalFilename);
      resource.setOriginalName(originalFilename);
      resource.setFilePath(filePath.toString());
      resource.setFileType(fileType);
      resource.setFileSize(file.getSize());
      resource.setMimeType(file.getContentType());
      resource.setDescription(description);
      resource.setTenantId(tenantId);

      int result = resourceService.create(resource);
      if (result > 0 && resource.getId() != null) {
        // 更新URL（使用插入后生成的ID）
        resource.setUrl(urlPrefix + resource.getId());
        resourceService.update(resource);

        ResourceResponseDTO dto = resourceService.getDTO(resource.getId());
        return Ret.data(dto);
      } else {
        // 如果插入失败，删除已上传的文件
        Files.deleteIfExists(filePath);
        return Ret.fail(500, "resource.upload.failed");
      }
    } catch (IOException e) {
      log.error("文件上传失败", e);
      return Ret.fail(500, "resource.upload.error");
    }
  }

  /**
   * 文件下载
   *
   * @param id 资源ID
   * @return 文件响应
   */
  @GetMapping("/download/{id}")
  public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long id) {
    SysResource resource = resourceService.get(id);
    if (resource == null) {
      return ResponseEntity.notFound().build();
    }

    try {
      File file = new File(resource.getFilePath());
      if (!file.exists() || !file.isFile()) {
        return ResponseEntity.notFound().build();
      }

      FileSystemResource fileResource = new FileSystemResource(file);
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                  encodeFileName(resource.getOriginalName()) + "\"");
      
      MediaType mediaType = MediaType.parseMediaType(
          StringUtils.hasText(resource.getMimeType()) ? resource.getMimeType() : "application/octet-stream");
      headers.setContentType(mediaType);
      headers.setContentLength(resource.getFileSize());

      return ResponseEntity.ok()
          .headers(headers)
          .body(fileResource);
    } catch (Exception e) {
      log.error("文件下载失败", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 文件预览（用于图片等可以直接在浏览器中显示的文件）
   *
   * @param id 资源ID
   * @return 文件响应
   */
  @GetMapping("/preview/{id}")
  public ResponseEntity<org.springframework.core.io.Resource> preview(@PathVariable Long id) {
    SysResource resource = resourceService.get(id);
    if (resource == null) {
      return ResponseEntity.notFound().build();
    }

    try {
      File file = new File(resource.getFilePath());
      if (!file.exists() || !file.isFile()) {
        return ResponseEntity.notFound().build();
      }

      FileSystemResource fileResource = new FileSystemResource(file);
      HttpHeaders headers = new HttpHeaders();
      MediaType mediaType = MediaType.parseMediaType(
          StringUtils.hasText(resource.getMimeType()) ? resource.getMimeType() : "application/octet-stream");
      headers.setContentType(mediaType);
      headers.setContentLength(resource.getFileSize());

      return ResponseEntity.ok()
          .headers(headers)
          .body(fileResource);
    } catch (Exception e) {
      log.error("文件预览失败", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 删除资源
   */
  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    SysResource resource = resourceService.get(id);
    if (resource == null) {
      return Ret.fail(404, "resource.not.found");
    }

    // 删除文件
    try {
      File file = new File(resource.getFilePath());
      if (file.exists()) {
        Files.deleteIfExists(file.toPath());
      }
    } catch (IOException e) {
      log.warn("删除文件失败: {}", resource.getFilePath(), e);
    }

    // 删除数据库记录
    return Ret.data(resourceService.delete(id));
  }

  /**
   * 获取文件扩展名
   */
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
  }

  /**
   * 确定文件类型
   */
  private String determineFileType(String mimeType, String extension) {
    if (mimeType != null) {
      if (mimeType.startsWith("image/")) {
        return "IMAGE";
      } else if (mimeType.startsWith("video/")) {
        return "VIDEO";
      } else if (mimeType.startsWith("audio/")) {
        return "AUDIO";
      } else if (mimeType.contains("pdf") || mimeType.contains("word") || 
                 mimeType.contains("excel") || mimeType.contains("text") ||
                 mimeType.contains("document")) {
        return "DOCUMENT";
      }
    }

    // 根据扩展名判断
    String ext = extension.toLowerCase();
    if (ext.matches("\\.(jpg|jpeg|png|gif|bmp|webp|svg)")) {
      return "IMAGE";
    } else if (ext.matches("\\.(mp4|avi|mov|wmv|flv|mkv)")) {
      return "VIDEO";
    } else if (ext.matches("\\.(mp3|wav|flac|aac|ogg)")) {
      return "AUDIO";
    } else if (ext.matches("\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt)")) {
      return "DOCUMENT";
    }

    return "OTHER";
  }

  /**
   * 编码文件名（用于HTTP响应头）
   */
  private String encodeFileName(String filename) {
    try {
      return java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
    } catch (Exception e) {
      return filename;
    }
  }
}

