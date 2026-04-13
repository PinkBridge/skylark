package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.ResourcePageRequest;
import cn.skylark.permission.authorization.dto.ResourceResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateResourceDTO;
import cn.skylark.permission.authorization.entity.SysResource;
import cn.skylark.permission.authorization.mapper.ResourceMapper;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源服务
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Service
public class ResourceService {

  @Resource
  private ResourceMapper resourceMapper;

  public SysResource get(Long id) {
    return resourceMapper.selectById(id);
  }

  public List<SysResource> list() {
    return resourceMapper.selectAll();
  }

  /**
   * 获取资源列表（DTO）
   *
   * @return 资源列表
   */
  public List<ResourceResponseDTO> listDTO() {
    List<SysResource> resources = resourceMapper.selectAll();
    return resources.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  public int create(SysResource resource) {
    // 自动设置租户ID
    if (resource.getTenantId() == null) {
      resource.setTenantId(TenantContext.getTenantId());
    }
    return resourceMapper.insert(resource);
  }

  public int update(SysResource resource) {
    return resourceMapper.update(resource);
  }

  public int delete(Long id) {
    return resourceMapper.deleteById(id);
  }

  /**
   * 获取资源信息（DTO）
   *
   * @param id 资源ID
   * @return 资源信息
   */
  public ResourceResponseDTO getDTO(Long id) {
    SysResource resource = resourceMapper.selectById(id);
    return resource != null ? convertToDTO(resource) : null;
  }

  /**
   * 分页查询资源列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<ResourceResponseDTO> pageDTOWithCondition(ResourcePageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getName()) ||
                          StringUtils.hasText(pageRequest.getOriginalName()) ||
                          StringUtils.hasText(pageRequest.getFileType()) ||
                          pageRequest.getCreateTime() != null;

    List<SysResource> records;
    Long total;

    if (hasCondition) {
      records = resourceMapper.selectPageWithCondition(
          pageRequest.getName(),
          pageRequest.getOriginalName(),
          pageRequest.getFileType(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = resourceMapper.countWithCondition(
          pageRequest.getName(),
          pageRequest.getOriginalName(),
          pageRequest.getFileType(),
          pageRequest.getCreateTime()
      );
    } else {
      records = resourceMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = resourceMapper.countAll();
    }

    List<ResourceResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新资源信息
   *
   * @param resourceId 资源ID
   * @param dto        更新资源信息DTO
   * @return 更新行数
   */
  public int updateResourceInfo(Long resourceId, UpdateResourceDTO dto) {
    return resourceMapper.updateResourceInfo(resourceId, dto);
  }

  /**
   * 将SysResource转换为ResourceResponseDTO
   *
   * @param resource 资源实体
   * @return 资源响应DTO
   */
  private ResourceResponseDTO convertToDTO(SysResource resource) {
    if (resource == null) {
      return null;
    }
    ResourceResponseDTO dto = new ResourceResponseDTO();
    BeanUtils.copyProperties(resource, dto);
    return dto;
  }
}

