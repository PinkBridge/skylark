package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.OrganizationPageRequest;
import cn.skylark.permission.authorization.dto.OrganizationResponseDTO;
import cn.skylark.permission.authorization.dto.OrganizationTreeNode;
import cn.skylark.permission.authorization.dto.ParentOrganizationDTO;
import cn.skylark.permission.authorization.dto.UpdateOrganizationDTO;
import cn.skylark.permission.authorization.entity.SysOrganization;
import cn.skylark.permission.authorization.entity.SysTenant;
import cn.skylark.permission.authorization.mapper.OrganizationMapper;
import cn.skylark.permission.authorization.mapper.TenantMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织服务
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Service
public class OrganizationService {

  @Resource
  private OrganizationMapper organizationMapper;

  @Resource
  private TenantMapper tenantMapper;

  public SysOrganization get(Long id) {
    return organizationMapper.selectById(id);
  }

  public List<SysOrganization> list() {
    return organizationMapper.selectAll();
  }

  /**
   * 获取组织列表（DTO）
   *
   * @return 组织列表
   */
  public List<OrganizationResponseDTO> listDTO() {
    List<SysOrganization> organizations = organizationMapper.selectAll();
    Map<Long, String> tenantNames = buildTenantNameMap();
    // 构建组织ID到组织的映射，用于快速查找父组织
    Map<Long, SysOrganization> organizationMap = new HashMap<>();
    for (SysOrganization org : organizations) {
      organizationMap.put(org.getId(), org);
    }
    return organizations.stream().map(org -> convertToDTO(org, organizationMap, tenantNames)).collect(Collectors.toList());
  }

  public int create(SysOrganization organization) {
    if (organization.getTenantId() == null) {
      organization.setTenantId(TenantContext.getTenantId());
    }
    return organizationMapper.insert(organization);
  }

  public int update(SysOrganization organization) {
    return organizationMapper.update(organization);
  }

  public int delete(Long id) {
    return organizationMapper.deleteById(id);
  }

  /**
   * 获取组织信息（DTO）
   *
   * @param id 组织ID
   * @return 组织信息
   */
  public OrganizationResponseDTO getDTO(Long id) {
    SysOrganization organization = organizationMapper.selectById(id);
    if (organization == null) {
      return null;
    }
    Map<Long, String> tenantNames = buildTenantNameMap();
    // 如果有父组织，查询父组织信息
    Map<Long, SysOrganization> organizationMap = new HashMap<>();
    if (organization.getParentId() != null) {
      SysOrganization parentOrg = organizationMapper.selectById(organization.getParentId());
      if (parentOrg != null) {
        organizationMap.put(parentOrg.getId(), parentOrg);
      }
    }
    return convertToDTO(organization, organizationMap, tenantNames);
  }

  /**
   * 分页查询组织列表（DTO）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<OrganizationResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysOrganization> records = organizationMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = organizationMapper.countAll();
    Map<Long, String> tenantNames = buildTenantNameMap();
    // 构建组织映射
    Map<Long, SysOrganization> organizationMap = new HashMap<>();
    for (SysOrganization org : records) {
      organizationMap.put(org.getId(), org);
    }
    // 添加父组织到映射中
    for (SysOrganization org : records) {
      if (org.getParentId() != null && !organizationMap.containsKey(org.getParentId())) {
        SysOrganization parentOrg = organizationMapper.selectById(org.getParentId());
        if (parentOrg != null) {
          organizationMap.put(parentOrg.getId(), parentOrg);
        }
      }
    }
    List<OrganizationResponseDTO> dtoList = records.stream().map(org -> convertToDTO(org, organizationMap, tenantNames)).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询组织列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<OrganizationResponseDTO> pageDTOWithCondition(OrganizationPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getName()) ||
                           StringUtils.hasText(pageRequest.getCode()) ||
                           StringUtils.hasText(pageRequest.getPhone()) ||
                           StringUtils.hasText(pageRequest.getEmail()) ||
                           StringUtils.hasText(pageRequest.getType()) ||
                           StringUtils.hasText(pageRequest.getStatus()) ||
                           pageRequest.getCreateTime() != null;

    List<SysOrganization> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = organizationMapper.selectPageWithCondition(
          pageRequest.getName(),
          pageRequest.getCode(),
          pageRequest.getPhone(),
          pageRequest.getEmail(),
          pageRequest.getType(),
          pageRequest.getStatus(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = organizationMapper.countWithCondition(
          pageRequest.getName(),
          pageRequest.getCode(),
          pageRequest.getPhone(),
          pageRequest.getEmail(),
          pageRequest.getType(),
          pageRequest.getStatus(),
          pageRequest.getCreateTime()
      );
    } else {
      // 使用无条件的查询
      records = organizationMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = organizationMapper.countAll();
    }

    Map<Long, String> tenantNames = buildTenantNameMap();
    // 构建组织映射
    Map<Long, SysOrganization> organizationMap = new HashMap<>();
    for (SysOrganization org : records) {
      organizationMap.put(org.getId(), org);
    }
    // 添加父组织到映射中
    for (SysOrganization org : records) {
      if (org.getParentId() != null && !organizationMap.containsKey(org.getParentId())) {
        SysOrganization parentOrg = organizationMapper.selectById(org.getParentId());
        if (parentOrg != null) {
          organizationMap.put(parentOrg.getId(), parentOrg);
        }
      }
    }

    List<OrganizationResponseDTO> dtoList = records.stream().map(org -> convertToDTO(org, organizationMap, tenantNames)).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新组织信息
   *
   * @param organizationId 组织ID
   * @param dto            更新组织信息DTO
   * @return 更新行数
   */
  public int updateOrganizationInfo(Long organizationId, UpdateOrganizationDTO dto) {
    return organizationMapper.updateOrganizationInfo(organizationId, dto);
  }

  private Map<Long, String> buildTenantNameMap() {
    Map<Long, String> map = new HashMap<>();
    List<SysTenant> tenants = tenantMapper.selectAll();
    if (tenants == null) {
      return map;
    }
    for (SysTenant t : tenants) {
      if (t != null && t.getId() != null) {
        map.put(t.getId(), t.getName());
      }
    }
    return map;
  }

  private OrganizationResponseDTO convertToDTO(SysOrganization organization, Map<Long, SysOrganization> organizationMap,
                                                 Map<Long, String> tenantNames) {
    if (organization == null) {
      return null;
    }
    OrganizationResponseDTO dto = new OrganizationResponseDTO();
    BeanUtils.copyProperties(organization, dto);
    if (organization.getTenantId() != null && tenantNames != null) {
      dto.setTenantName(tenantNames.get(organization.getTenantId()));
    }

    // 设置父组织信息
    if (organization.getParentId() != null && organizationMap.containsKey(organization.getParentId())) {
      SysOrganization parentOrg = organizationMap.get(organization.getParentId());
      dto.setParentOrganization(convertToParentOrganizationDTO(parentOrg));
    }

    return dto;
  }

  /**
   * 将SysOrganization转换为ParentOrganizationDTO
   *
   * @param organization 组织实体
   * @return 父组织DTO
   */
  private ParentOrganizationDTO convertToParentOrganizationDTO(SysOrganization organization) {
    if (organization == null) {
      return null;
    }
    ParentOrganizationDTO dto = new ParentOrganizationDTO();
    dto.setId(organization.getId());
    dto.setName(organization.getName());
    dto.setCode(organization.getCode());
    dto.setType(organization.getType());
    return dto;
  }

  /**
   * 获取组织树
   *
   * @param name 组织名称（模糊搜索，可选）
   * @return 组织树
   */
  public List<OrganizationTreeNode> organizationTree(String name) {
    List<SysOrganization> organizations = organizationMapper.selectAll();
    if (StringUtils.hasText(name)) {
      organizations = filterOrganizationsByName(organizations, name);
    }
    Map<Long, String> tenantNames = buildTenantNameMap();
    return buildTree(organizations, tenantNames);
  }

  /**
   * 根据名称过滤组织，并保留树结构（包含匹配节点的所有父节点和子节点）
   *
   * @param organizations 组织列表
   * @param name          搜索名称
   * @return 过滤后的组织列表
   */
  private List<SysOrganization> filterOrganizationsByName(List<SysOrganization> organizations, String name) {
    if (organizations == null || organizations.isEmpty()) {
      return organizations;
    }

    // 构建组织映射
    Map<Long, SysOrganization> organizationMap = new HashMap<>();
    for (SysOrganization org : organizations) {
      organizationMap.put(org.getId(), org);
    }

    // 找出名称匹配的组织
    Set<Long> matchedOrgIds = new HashSet<>();
    for (SysOrganization org : organizations) {
      if (org.getName() != null && org.getName().contains(name)) {
        matchedOrgIds.add(org.getId());
      }
    }

    if (matchedOrgIds.isEmpty()) {
      return Collections.emptyList();
    }

    // 收集需要包含的组织ID（匹配的组织 + 所有父节点 + 所有子节点）
    Set<Long> includedOrgIds = new HashSet<>(matchedOrgIds);

    // 添加所有父节点
    for (Long orgId : matchedOrgIds) {
      addParentOrganizations(orgId, organizationMap, includedOrgIds);
    }

    // 添加所有子节点
    for (Long orgId : matchedOrgIds) {
      addChildOrganizations(orgId, organizationMap, includedOrgIds);
    }

    // 返回过滤后的组织列表
    return organizations.stream()
        .filter(org -> includedOrgIds.contains(org.getId()))
        .collect(Collectors.toList());
  }

  /**
   * 递归添加父组织
   *
   * @param orgId          组织ID
   * @param organizationMap 组织映射
   * @param includedOrgIds 已包含的组织ID集合
   */
  private void addParentOrganizations(Long orgId, Map<Long, SysOrganization> organizationMap, Set<Long> includedOrgIds) {
    SysOrganization org = organizationMap.get(orgId);
    if (org == null || org.getParentId() == null) {
      return;
    }
    if (!includedOrgIds.contains(org.getParentId())) {
      includedOrgIds.add(org.getParentId());
      addParentOrganizations(org.getParentId(), organizationMap, includedOrgIds);
    }
  }

  /**
   * 递归添加子组织
   *
   * @param orgId          组织ID
   * @param organizationMap 组织映射
   * @param includedOrgIds 已包含的组织ID集合
   */
  private void addChildOrganizations(Long orgId, Map<Long, SysOrganization> organizationMap, Set<Long> includedOrgIds) {
    for (SysOrganization org : organizationMap.values()) {
      if (org.getParentId() != null && org.getParentId().equals(orgId)) {
        if (!includedOrgIds.contains(org.getId())) {
          includedOrgIds.add(org.getId());
          addChildOrganizations(org.getId(), organizationMap, includedOrgIds);
        }
      }
    }
  }

  /**
   * 构建组织树
   *
   * @param organizations 组织列表
   * @return 组织树
   */
  private List<OrganizationTreeNode> buildTree(List<SysOrganization> organizations, Map<Long, String> tenantNames) {
    // 构建组织ID到组织实体的映射，用于查找父组织
    Map<Long, SysOrganization> organizationMap = new HashMap<>();
    for (SysOrganization org : organizations) {
      organizationMap.put(org.getId(), org);
    }

    Map<Long, OrganizationTreeNode> map = new LinkedHashMap<>();
    for (SysOrganization org : organizations) {
      OrganizationTreeNode node = toNode(org, organizationMap, tenantNames);
      map.put(node.getId(), node);
    }

    List<OrganizationTreeNode> roots = new ArrayList<>();
    for (OrganizationTreeNode node : map.values()) {
      if (node.getParentId() == null || !map.containsKey(node.getParentId())) {
        roots.add(node);
      } else {
        map.get(node.getParentId()).getChildren().add(node);
      }
    }

    // 按sort排序
    sortTree(roots);
    return roots;
  }

  /**
   * 将SysOrganization转换为OrganizationTreeNode
   *
   * @param organization      组织实体
   * @param organizationMap   组织映射（用于查找父组织）
   * @return 组织树节点
   */
  private OrganizationTreeNode toNode(SysOrganization organization, Map<Long, SysOrganization> organizationMap,
                                      Map<Long, String> tenantNames) {
    OrganizationTreeNode node = new OrganizationTreeNode();
    BeanUtils.copyProperties(organization, node);
    if (organization.getTenantId() != null && tenantNames != null) {
      node.setTenantName(tenantNames.get(organization.getTenantId()));
    }

    // 设置父组织信息
    if (organization.getParentId() != null && organizationMap.containsKey(organization.getParentId())) {
      SysOrganization parentOrg = organizationMap.get(organization.getParentId());
      node.setParentOrganization(convertToParentOrganizationDTO(parentOrg));
    }

    return node;
  }

  /**
   * 递归排序树节点
   *
   * @param nodes 树节点列表
   */
  private void sortTree(List<OrganizationTreeNode> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      return;
    }
    // 按sort字段排序，如果sort为null则按id排序
    nodes.sort((a, b) -> {
      Integer sortA = a.getSort();
      Integer sortB = b.getSort();
      if (sortA != null && sortB != null) {
        return sortA.compareTo(sortB);
      } else if (sortA != null) {
        return -1;
      } else if (sortB != null) {
        return 1;
      } else {
        return Long.compare(a.getId(), b.getId());
      }
    });
    // 递归排序子节点
    for (OrganizationTreeNode node : nodes) {
      if (node.getChildren() != null && !node.getChildren().isEmpty()) {
        sortTree(node.getChildren());
      }
    }
  }

  /**
   * 同租户下，指定根组织及其所有子孙组织的 id（含根）。
   */
  public Set<Long> collectOrgIdsInSubtree(Long rootOrgId, Long tenantId) {
    if (rootOrgId == null || tenantId == null) {
      return Collections.emptySet();
    }
    List<SysOrganization> all = organizationMapper.selectAll();
    Map<Long, List<Long>> byParent = new HashMap<>();
    for (SysOrganization o : all) {
      if (o.getTenantId() == null || !tenantId.equals(o.getTenantId())) {
        continue;
      }
      Long pid = o.getParentId() == null ? Long.valueOf(0L) : o.getParentId();
      byParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(o.getId());
    }
    Set<Long> out = new LinkedHashSet<>();
    Deque<Long> q = new ArrayDeque<>();
    q.add(rootOrgId);
    while (!q.isEmpty()) {
      Long id = q.poll();
      if (!out.add(id)) {
        continue;
      }
      for (Long c : byParent.getOrDefault(id, Collections.emptyList())) {
        q.add(c);
      }
    }
    return out;
  }

  /**
   * 同租户下，指定父组织的直接子组织 id。
   */
  public Set<Long> collectDirectChildOrgIds(Long parentOrgId, Long tenantId) {
    if (parentOrgId == null || tenantId == null) {
      return Collections.emptySet();
    }
    List<SysOrganization> all = organizationMapper.selectAll();
    Set<Long> out = new LinkedHashSet<>();
    for (SysOrganization o : all) {
      if (o.getTenantId() == null || !tenantId.equals(o.getTenantId())) {
        continue;
      }
      if (parentOrgId.equals(o.getParentId())) {
        out.add(o.getId());
      }
    }
    return out;
  }
}

