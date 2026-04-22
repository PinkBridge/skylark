package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {
  SysMenu selectById(@Param("id") Long id);

  List<SysMenu> selectAll();

  int insert(SysMenu menu);

  int update(SysMenu menu);

  int deleteById(@Param("id") Long id);

  List<SysMenu> selectByRoleId(@Param("roleId") Long roleId);

  int deleteBindingsByRoleId(@Param("roleId") Long roleId);

  int bindRoleMenus(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

  /**
   * 检查角色和菜单的关联是否存在
   *
   * @param roleId 角色ID
   * @param menuId 菜单ID
   * @return 存在返回1，不存在返回0
   */
  int existsRoleMenuBinding(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

  /**
   * 删除角色和菜单的单个关联
   *
   * @param roleId 角色ID
   * @param menuId 菜单ID
   * @return 删除行数
   */
  int deleteRoleMenuBinding(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

  /**
   * 插入角色和菜单的单个关联
   *
   * @param roleId 角色ID
   * @param menuId 菜单ID
   * @return 插入行数
   */
  int insertRoleMenuBinding(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

  /**
   * @param appCode 非空时只返回该应用下的菜单
   */
  List<SysMenu> selectMenusByUsername(@Param("username") String username,
                                      @Param("appCode") String appCode);

  /**
   * 更新菜单信息
   *
   * @param menuId 菜单ID
   * @param menu   菜单信息
   * @return 更新行数
   */
  int updateMenuInfo(@Param("menuId") Long menuId, @Param("menu") cn.skylark.permission.authorization.dto.UpdateMenuDTO menu);

  /**
   * 按权限标识查询菜单ID
   */
  List<Long> selectIdsByPermlabels(@Param("permlabels") List<String> permlabels);

  /**
   * 回收：删除角色绑定中“不在允许集合”里的菜单。
   * @param roleId 角色ID
   * @param allowedMenuIds 允许的菜单ID集合（为空表示全部删除）
   * @return 删除行数
   */
  int deleteRoleMenusNotInAllowed(@Param("roleId") Long roleId, @Param("allowedMenuIds") List<Long> allowedMenuIds);
}
