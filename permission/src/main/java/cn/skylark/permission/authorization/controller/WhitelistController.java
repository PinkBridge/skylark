package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.UpdateWhitelistDTO;
import cn.skylark.permission.authorization.dto.WhitelistPageRequest;
import cn.skylark.permission.authorization.dto.WhitelistResponseDTO;
import cn.skylark.permission.authorization.entity.SysWhitelist;
import cn.skylark.permission.authorization.service.RbacService;
import cn.skylark.permission.authorization.service.WhitelistService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 白名单控制器
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@RestController
@RequestMapping("/api/permission/whitelist")
public class WhitelistController {

  @Resource
  private WhitelistService whitelistService;

  @Resource
  private RbacService rbacService;

  @GetMapping
  public Ret<List<WhitelistResponseDTO>> list() {
    return Ret.data(whitelistService.listDTO());
  }

  @GetMapping("/{id}")
  public Ret<WhitelistResponseDTO> get(@PathVariable Long id) {
    WhitelistResponseDTO whitelistDTO = whitelistService.getDTO(id);
    if (whitelistDTO == null) {
      return Ret.fail(404, "whitelist.not.found");
    }
    return Ret.data(whitelistDTO);
  }

  /**
   * 分页查询白名单列表（支持搜索）
   *
   * @param page       页码，从1开始，默认1
   * @param size       每页大小，默认10
   * @param method     HTTP方法（模糊搜索）
   * @param path       API路径（模糊搜索）
   * @param remark     备注（模糊搜索）
   * @param enabled    是否启用（精确搜索）
   * @param createTime 创建时间（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<WhitelistResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String path,
      @RequestParam(required = false) String remark,
      @RequestParam(required = false) Boolean enabled,
      @RequestParam(required = false) LocalDateTime createTime) {
    WhitelistPageRequest pageRequest = new WhitelistPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setMethod(method);
    pageRequest.setPath(path);
    pageRequest.setRemark(remark);
    pageRequest.setEnabled(enabled);
    pageRequest.setCreateTime(createTime);
    return Ret.data(whitelistService.pageDTOWithCondition(pageRequest));
  }

  @PostMapping
  public Ret<Integer> create(@RequestBody SysWhitelist whitelist) {
    int result = whitelistService.create(whitelist);
    // 创建后刷新缓存
    rbacService.refreshWhitelistCache();
    return Ret.data(result);
  }

  @PutMapping("/{id}")
  public Ret<Integer> update(@PathVariable Long id, @RequestBody UpdateWhitelistDTO updateWhitelistDTO) {
    WhitelistResponseDTO whitelistDTO = whitelistService.getDTO(id);
    if (whitelistDTO == null) {
      return Ret.fail(404, "whitelist.not.found");
    }
    int result = whitelistService.updateWhitelistInfo(id, updateWhitelistDTO);
    // 更新后刷新缓存
    rbacService.refreshWhitelistCache();
    return Ret.data(result);
  }

  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    int result = whitelistService.delete(id);
    // 删除后刷新缓存
    rbacService.refreshWhitelistCache();
    return Ret.data(result);
  }

  /**
   * 手动刷新白名单缓存
   */
  @PostMapping("/refresh-cache")
  public Ret<Void> refreshCache() {
    rbacService.refreshWhitelistCache();
    return Ret.ok();
  }
}

