package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.MenuResponseDTO;
import cn.skylark.permission.authorization.dto.MenuTreeNode;
import cn.skylark.permission.authorization.dto.UpdateMenuDTO;
import cn.skylark.permission.authorization.entity.SysMenu;
import cn.skylark.permission.authorization.mapper.MenuMapper;
import cn.skylark.permission.authorization.support.MenuI18nResolver;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

  @Resource
  private MenuMapper menuMapper;

  @Resource
  private MenuI18nResolver menuI18nResolver;

  @Resource
  private TenantRolePermissionGuard tenantRolePermissionGuard;

  public SysMenu get(Long id) {
    return menuMapper.selectById(id);
  }

  public List<SysMenu> list() {
    return menuMapper.selectAll();
  }

  public int create(SysMenu menu) {
    if (menu != null && !StringUtils.hasText(menu.getNameI18n()) && StringUtils.hasText(menu.getName())) {
      menu.setNameI18n(menuI18nResolver.defaultNameI18nJson(menu.getName()));
    }
    return menuMapper.insert(menu);
  }

  public int update(SysMenu menu) {
    return menuMapper.update(menu);
  }

  public int delete(Long id) {
    return menuMapper.deleteById(id);
  }

  public List<SysMenu> listByRole(Long roleId) {
    return menuMapper.selectByRoleId(roleId);
  }

  public void bindRoleMenus(Long roleId, List<Long> menuIds) {
    tenantRolePermissionGuard.validateRoleMenusWithinCeiling(roleId, menuIds);
    menuMapper.deleteBindingsByRoleId(roleId);
    if (menuIds != null && !menuIds.isEmpty()) {
      menuMapper.bindRoleMenus(roleId, menuIds);
    }
  }

  /**
   * 批量切换角色和菜单的关联状态
   * 对于每个菜单ID，如果关联不存在则添加，如果存在则删除
   *
   * @param roleId  角色ID
   * @param menuIds 菜单ID数组
   * @return 操作结果，key为菜单ID，value为true表示添加了关联，false表示删除了关联
   */
  public Map<Long, Boolean> toggleRoleMenuBindings(Long roleId, List<Long> menuIds) {
    Map<Long, Boolean> results = new HashMap<>();
    if (menuIds == null || menuIds.isEmpty()) {
      return results;
    }
    tenantRolePermissionGuard.validateRoleMenusWithinCeiling(roleId, menuIds);
    for (Long menuId : menuIds) {
      int exists = menuMapper.existsRoleMenuBinding(roleId, menuId);
      if (exists > 0) {
        // 存在关联，删除
        menuMapper.deleteRoleMenuBinding(roleId, menuId);
        results.put(menuId, false);
      } else {
        // 不存在关联，添加
        menuMapper.insertRoleMenuBinding(roleId, menuId);
        results.put(menuId, true);
      }
    }
    return results;
  }

  /**
   * 获取菜单列表（DTO）
   *
   * @return 菜单列表
   */
  public List<MenuResponseDTO> listDTO(Locale locale) {
    List<SysMenu> menus = menuMapper.selectAll();
    // 构建菜单ID到菜单的映射，用于快速查找父菜单
    Map<Long, SysMenu> menuMap = new HashMap<>();
    for (SysMenu menu : menus) {
      menuMap.put(menu.getId(), menu);
    }
    return menus.stream().map(menu -> convertToDTO(menu, menuMap, locale)).collect(java.util.stream.Collectors.toList());
  }

  /**
   * 获取菜单信息（DTO）
   *
   * @param id 菜单ID
   * @return 菜单信息
   */
  public MenuResponseDTO getDTO(Long id, Locale locale) {
    SysMenu menu = menuMapper.selectById(id);
    if (menu == null) {
      return null;
    }
    // 如果有父菜单，查询父菜单信息
    Map<Long, SysMenu> menuMap = new HashMap<>();
    if (menu.getParentId() != null) {
      SysMenu parentMenu = menuMapper.selectById(menu.getParentId());
      if (parentMenu != null) {
        menuMap.put(parentMenu.getId(), parentMenu);
      }
    }
    return convertToDTO(menu, menuMap, locale);
  }

  /**
   * 更新菜单信息
   *
   * @param menuId 菜单ID
   * @param dto    更新菜单信息DTO
   * @return 更新行数
   */
  public int updateMenuInfo(Long menuId, UpdateMenuDTO dto) {
    if (dto != null && !StringUtils.hasText(dto.getNameI18n())) {
      SysMenu existing = menuMapper.selectById(menuId);
      if (existing != null && StringUtils.hasText(existing.getNameI18n())) {
        dto.setNameI18n(existing.getNameI18n());
      }
    }
    return menuMapper.updateMenuInfo(menuId, dto);
  }

  /**
   * 获取所有菜单树（不区分用户）
   *
   * @param name      菜单名称（模糊搜索，可选）
   * @param permlabel 权限标签（模糊搜索，可选）
   * @param moduleKey 模块标识（模糊搜索，可选）
   * @param path      菜单路径（模糊搜索，可选）
   * @return 菜单树
   */
  public List<MenuTreeNode> menuTree(String name, String permlabel, String moduleKey, String path, Locale locale) {
    List<SysMenu> menus = menuMapper.selectAll();
    if (StringUtils.hasText(name) || StringUtils.hasText(permlabel) || 
        StringUtils.hasText(moduleKey) || StringUtils.hasText(path)) {
      menus = filterMenus(menus, name, permlabel, moduleKey, path);
    }
    return buildTree(menus, locale);
  }

  /**
   * 获取当前用户的菜单树
   *
   * @param username  用户名
   * @param name      菜单名称（模糊搜索，可选）
   * @param permlabel 权限标签（模糊搜索，可选）
   * @param moduleKey 模块标识（模糊搜索，可选）
   * @param path      菜单路径（模糊搜索，可选）
   * @return 菜单树
   */
  public List<MenuTreeNode> userMenuTree(String username, String name, String permlabel, 
                                          String moduleKey, String path, Locale locale) {
    List<SysMenu> menus = menuMapper.selectMenusByUsername(username);
    if (StringUtils.hasText(name) || StringUtils.hasText(permlabel) || 
        StringUtils.hasText(moduleKey) || StringUtils.hasText(path)) {
      menus = filterMenus(menus, name, permlabel, moduleKey, path);
    }
    return buildTree(menus, locale);
  }

  /**
   * 按菜单ID集合生成菜单树（同时包含必要的父节点），用于“可授权范围”展示。
   */
  public List<MenuTreeNode> menuTreeByIds(Set<Long> menuIds, Locale locale) {
    if (menuIds == null || menuIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<SysMenu> all = menuMapper.selectAll();
    if (all == null || all.isEmpty()) {
      return Collections.emptyList();
    }
    Map<Long, SysMenu> map = new HashMap<>();
    for (SysMenu m : all) {
      map.put(m.getId(), m);
    }
    Set<Long> included = new HashSet<>();
    for (Long id : menuIds) {
      addWithParents(id, map, included);
    }
    List<SysMenu> filtered = all.stream().filter(m -> m != null && included.contains(m.getId())).collect(Collectors.toList());
    return buildTree(filtered, locale);
  }

  private void addWithParents(Long id, Map<Long, SysMenu> map, Set<Long> included) {
    if (id == null || included.contains(id)) {
      return;
    }
    SysMenu m = map.get(id);
    if (m == null) {
      return;
    }
    included.add(id);
    if (m.getParentId() != null) {
      addWithParents(m.getParentId(), map, included);
    }
  }

  /**
   * 根据多个字段过滤菜单，并保留树结构（包含匹配节点的所有父节点和子节点）
   *
   * @param menus    菜单列表
   * @param name      菜单名称（模糊搜索，可选）
   * @param permlabel 权限标签（模糊搜索，可选）
   * @param moduleKey 模块标识（模糊搜索，可选）
   * @param path      菜单路径（模糊搜索，可选）
   * @return 过滤后的菜单列表
   */
  private List<SysMenu> filterMenus(List<SysMenu> menus, String name, String permlabel, 
                                     String moduleKey, String path) {
    if (menus == null || menus.isEmpty()) {
      return menus;
    }

    // 构建菜单映射
    Map<Long, SysMenu> menuMap = new HashMap<>();
    for (SysMenu menu : menus) {
      menuMap.put(menu.getId(), menu);
    }

    // 找出匹配的菜单（任一字段匹配即可）
    Set<Long> matchedMenuIds = new HashSet<>();
    for (SysMenu menu : menus) {
      boolean matched = false;
      
      // 检查名称
      if (StringUtils.hasText(name) && menu.getName() != null && menu.getName().contains(name)) {
        matched = true;
      }
      // 检查权限标签
      if (!matched && StringUtils.hasText(permlabel) && menu.getPermlabel() != null && 
          menu.getPermlabel().contains(permlabel)) {
        matched = true;
      }
      // 检查模块标识
      if (!matched && StringUtils.hasText(moduleKey) && menu.getModuleKey() != null && 
          menu.getModuleKey().contains(moduleKey)) {
        matched = true;
      }
      // 检查路径
      if (!matched && StringUtils.hasText(path) && menu.getPath() != null && 
          menu.getPath().contains(path)) {
        matched = true;
      }
      
      if (matched) {
        matchedMenuIds.add(menu.getId());
      }
    }

    if (matchedMenuIds.isEmpty()) {
      return Collections.emptyList();
    }

    // 收集需要包含的菜单ID（匹配的菜单 + 所有父节点 + 所有子节点）
    Set<Long> includedMenuIds = new HashSet<>(matchedMenuIds);

    // 添加所有父节点
    for (Long menuId : matchedMenuIds) {
      addParentMenus(menuId, menuMap, includedMenuIds);
    }

    // 添加所有子节点
    for (Long menuId : matchedMenuIds) {
      addChildMenus(menuId, menuMap, includedMenuIds);
    }

    // 返回过滤后的菜单列表
    return menus.stream()
            .filter(menu -> includedMenuIds.contains(menu.getId()))
            .collect(Collectors.toList());
  }

  /**
   * 递归添加父菜单
   *
   * @param menuId         菜单ID
   * @param menuMap        菜单映射
   * @param includedMenuIds 已包含的菜单ID集合
   */
  private void addParentMenus(Long menuId, Map<Long, SysMenu> menuMap, Set<Long> includedMenuIds) {
    SysMenu menu = menuMap.get(menuId);
    if (menu == null || menu.getParentId() == null) {
      return;
    }
    if (!includedMenuIds.contains(menu.getParentId())) {
      includedMenuIds.add(menu.getParentId());
      addParentMenus(menu.getParentId(), menuMap, includedMenuIds);
    }
  }

  /**
   * 递归添加子菜单
   *
   * @param menuId         菜单ID
   * @param menuMap        菜单映射
   * @param includedMenuIds 已包含的菜单ID集合
   */
  private void addChildMenus(Long menuId, Map<Long, SysMenu> menuMap, Set<Long> includedMenuIds) {
    for (SysMenu menu : menuMap.values()) {
      if (menu.getParentId() != null && menu.getParentId().equals(menuId)) {
        if (!includedMenuIds.contains(menu.getId())) {
          includedMenuIds.add(menu.getId());
          addChildMenus(menu.getId(), menuMap, includedMenuIds);
        }
      }
    }
  }

  /**
   * 构建菜单树
   *
   * @param menus 菜单列表
   * @return 菜单树
   */
  private List<MenuTreeNode> buildTree(List<SysMenu> menus, Locale locale) {
    // 构建菜单ID到菜单实体的映射，用于查找父菜单
    Map<Long, SysMenu> menuMap = new HashMap<>();
    for (SysMenu menu : menus) {
      menuMap.put(menu.getId(), menu);
    }
    
    Map<Long, MenuTreeNode> map = new LinkedHashMap<>();
    for (SysMenu m : menus) {
      MenuTreeNode n = toNode(m, menuMap, locale);
      map.put(n.getId(), n);
    }
    List<MenuTreeNode> roots = new ArrayList<>();
    for (MenuTreeNode n : map.values()) {
      if (n.getParentId() == null || !map.containsKey(n.getParentId())) {
        roots.add(n);
      } else {
        map.get(n.getParentId()).getChildren().add(n);
      }
    }
    sort(roots);
    return roots;
  }

  /**
   * 对菜单树进行排序
   *
   * @param list 菜单节点列表
   */
  private void sort(List<MenuTreeNode> list) {
    list.sort(Comparator.comparing(MenuTreeNode::getSort, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(MenuTreeNode::getId));
    for (MenuTreeNode n : list) {
      sort(n.getChildren());
    }
  }

  /**
   * 将SysMenu转换为MenuTreeNode
   *
   * @param m       菜单实体
   * @param menuMap 菜单映射（用于查找父菜单）
   * @return 菜单树节点
   */
  private MenuTreeNode toNode(SysMenu m, Map<Long, SysMenu> menuMap, Locale locale) {
    MenuTreeNode n = new MenuTreeNode();
    n.setId(m.getId());
    n.setParentId(m.getParentId());
    n.setName(menuI18nResolver.resolveDisplayName(m, locale));
    n.setPath(m.getPath());
    n.setIcon(m.getIcon());
    n.setSort(m.getSort());
    n.setHidden(m.getHidden());
    n.setType(m.getType());
    n.setPermlabel(m.getPermlabel());
    n.setModuleKey(m.getModuleKey());
    n.setCreateTime(m.getCreateTime());
    n.setUpdateTime(m.getUpdateTime());
    n.setChildren(new ArrayList<>());
    
    // 设置父菜单信息
    if (m.getParentId() != null && menuMap.containsKey(m.getParentId())) {
      SysMenu parentMenu = menuMap.get(m.getParentId());
      n.setParentMenu(convertToParentMenuDTO(parentMenu, locale));
    }
    
    return n;
  }

  /**
   * 将SysMenu转换为MenuResponseDTO
   *
   * @param menu    菜单实体
   * @param menuMap 菜单映射（用于查找父菜单）
   * @return 菜单响应DTO
   */
  private MenuResponseDTO convertToDTO(SysMenu menu, Map<Long, SysMenu> menuMap, Locale locale) {
    if (menu == null) {
      return null;
    }
    MenuResponseDTO dto = new MenuResponseDTO();
    BeanUtils.copyProperties(menu, dto);
    dto.setDisplayName(menuI18nResolver.resolveDisplayName(menu, locale));
    
    // 设置父菜单信息
    if (menu.getParentId() != null && menuMap.containsKey(menu.getParentId())) {
      SysMenu parentMenu = menuMap.get(menu.getParentId());
      dto.setParentMenu(convertToParentMenuDTO(parentMenu, locale));
    }
    
    return dto;
  }

  /**
   * 将SysMenu转换为ParentMenuDTO
   *
   * @param menu 菜单实体
   * @return 父菜单DTO
   */
  private cn.skylark.permission.authorization.dto.ParentMenuDTO convertToParentMenuDTO(SysMenu menu, Locale locale) {
    if (menu == null) {
      return null;
    }
    cn.skylark.permission.authorization.dto.ParentMenuDTO dto = new cn.skylark.permission.authorization.dto.ParentMenuDTO();
    dto.setId(menu.getId());
    dto.setName(menuI18nResolver.resolveDisplayName(menu, locale));
    dto.setPath(menu.getPath());
    dto.setIcon(menu.getIcon());
    dto.setType(menu.getType());
    dto.setPermlabel(menu.getPermlabel());
    dto.setModuleKey(menu.getModuleKey());
    return dto;
  }
}
