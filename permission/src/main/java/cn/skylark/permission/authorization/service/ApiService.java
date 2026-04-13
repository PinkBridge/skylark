package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.ApiPageRequest;
import cn.skylark.permission.authorization.dto.ApiResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateApiDTO;
import cn.skylark.permission.authorization.entity.SysApi;
import cn.skylark.permission.authorization.mapper.ApiMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiService {

  @Resource
  private ApiMapper apiMapper;

  @Resource
  private TenantRolePermissionGuard tenantRolePermissionGuard;

  public SysApi get(Long id) {
    return apiMapper.selectById(id);
  }

  public List<SysApi> list() {
    return apiMapper.selectAll();
  }

  public int create(SysApi api) {
    return apiMapper.insert(api);
  }

  public int update(SysApi api) {
    return apiMapper.update(api);
  }

  public int delete(Long id) {
    return apiMapper.deleteById(id);
  }

  public List<SysApi> listByRole(Long roleId) {
    return apiMapper.selectByRoleId(roleId);
  }

  public void bindRoleApis(Long roleId, List<Long> apiIds) {
    tenantRolePermissionGuard.validateRoleApisWithinCeiling(roleId, apiIds);
    apiMapper.deleteBindingsByRoleId(roleId);
    if (apiIds != null && !apiIds.isEmpty()) {
      apiMapper.bindRoleApis(roleId, apiIds);
    }
  }

  /**
   * 切换角色和API的关联状态
   * 如果关联不存在则添加，如果存在则删除
   *
   * @param roleId 角色ID
   * @param apiId  API ID
   * @return true-添加了关联，false-删除了关联
   */
  public boolean toggleRoleApiBinding(Long roleId, Long apiId) {
    tenantRolePermissionGuard.validateRoleApisWithinCeiling(roleId, java.util.Collections.singletonList(apiId));
    int exists = apiMapper.existsRoleApiBinding(roleId, apiId);
    if (exists > 0) {
      // 存在关联，删除
      apiMapper.deleteRoleApiBinding(roleId, apiId);
      return false;
    } else {
      // 不存在关联，添加
      apiMapper.insertRoleApiBinding(roleId, apiId);
      return true;
    }
  }

  public boolean isApiBoundToRoles(List<String> roleNames, String method, String path) {
    return isApiBoundToRolesInternal(roleNames, method, path, null);
  }

  public boolean isApiBoundToRolesByPermlabel(List<String> roleNames, String permlabel) {
    return isApiBoundToRolesInternal(roleNames, null, null, permlabel);
  }

  private boolean isApiBoundToRolesInternal(List<String> roleNames, String method, String path, String permlabel) {
    if (CollectionUtils.isEmpty(roleNames)) {
      return false;
    }
    if ((permlabel == null || permlabel.isEmpty()) && (method == null || path == null)) {
      return false;
    }
    return apiMapper.countByRoleNamesAndApi(roleNames, method, path, permlabel) > 0;
  }

  public List<SysApi> listByRoleNames(List<String> roleNames) {
    if (CollectionUtils.isEmpty(roleNames)) {
      return Collections.emptyList();
    }
    return apiMapper.selectByRoleNames(roleNames);
  }

  /**
   * 获取API列表（DTO）
   *
   * @return API列表
   */
  public List<ApiResponseDTO> listDTO() {
    List<SysApi> apis = apiMapper.selectAll();
    return apis.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  /**
   * 获取API信息（DTO）
   *
   * @param id API ID
   * @return API信息
   */
  public ApiResponseDTO getDTO(Long id) {
    SysApi api = apiMapper.selectById(id);
    return api != null ? convertToDTO(api) : null;
  }

  /**
   * 分页查询API列表（DTO）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<ApiResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysApi> records = apiMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = apiMapper.countAll();
    List<ApiResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询API列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<ApiResponseDTO> pageDTOWithCondition(ApiPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getMethod()) ||
                           StringUtils.hasText(pageRequest.getPath()) ||
                           StringUtils.hasText(pageRequest.getPermlabel()) ||
                           StringUtils.hasText(pageRequest.getModuleKey()) ||
                           pageRequest.getCreateTime() != null;

    List<SysApi> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = apiMapper.selectPageWithCondition(
          pageRequest.getMethod(),
          pageRequest.getPath(),
          pageRequest.getPermlabel(),
          pageRequest.getModuleKey(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = apiMapper.countWithCondition(
          pageRequest.getMethod(),
          pageRequest.getPath(),
          pageRequest.getPermlabel(),
          pageRequest.getModuleKey(),
          pageRequest.getCreateTime()
      );
    } else {
      // 使用无条件的查询
      records = apiMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = apiMapper.countAll();
    }

    List<ApiResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新API信息
   *
   * @param apiId API ID
   * @param dto   更新API信息DTO
   * @return 更新行数
   */
  public int updateApiInfo(Long apiId, UpdateApiDTO dto) {
    return apiMapper.updateApiInfo(apiId, dto);
  }

  /**
   * 将SysApi转换为ApiResponseDTO
   *
   * @param api API实体
   * @return API响应DTO
   */
  private ApiResponseDTO convertToDTO(SysApi api) {
    if (api == null) {
      return null;
    }
    ApiResponseDTO dto = new ApiResponseDTO();
    BeanUtils.copyProperties(api, dto);
    return dto;
  }
}
