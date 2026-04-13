package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.DataDomainPageRequest;
import cn.skylark.permission.authorization.dto.DataDomainResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateDataDomainDTO;
import cn.skylark.permission.authorization.entity.SysDataDomain;
import cn.skylark.permission.authorization.service.DataDomainService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据域控制器
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@RestController
@RequestMapping("/api/permission/data-domains")
public class DataDomainController {

  @Resource
  private DataDomainService dataDomainService;

  /**
   * 查询所有数据域
   *
   * @return 数据域列表
   */
  @GetMapping
  public Ret<List<DataDomainResponseDTO>> list() {
    return Ret.data(dataDomainService.listDTO());
  }

  /**
   * 租户侧可授权数据域（租户管理员已绑定的数据域）；平台上下文返回全量。
   */
  @GetMapping("/grantable")
  public Ret<List<DataDomainResponseDTO>> grantableList(
      @RequestParam(required = false) Long forRoleId) {
    return Ret.data(dataDomainService.listGrantableDTOs(forRoleId));
  }

  /**
   * 根据ID查询数据域
   *
   * @param id 数据域ID
   * @return 数据域信息
   */
  @GetMapping("/{id}")
  public Ret<DataDomainResponseDTO> get(@PathVariable Long id) {
    DataDomainResponseDTO dataDomainDTO = dataDomainService.getDTO(id);
    if (dataDomainDTO == null) {
      return Ret.fail(404, "data.domain.not.found");
    }
    return Ret.data(dataDomainDTO);
  }

  /**
   * 分页查询数据域列表（支持搜索）
   *
   * @param page       页码，从1开始，默认1
   * @param size       每页大小，默认10
   * @param name       数据域名称（模糊搜索）
   * @param code       数据域编码（模糊搜索）
   * @param type       数据范围类型
   * @param enabled    是否启用
   * @param createTime 创建时间（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<DataDomainResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Boolean enabled,
      @RequestParam(required = false) LocalDateTime createTime) {
    DataDomainPageRequest pageRequest = new DataDomainPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setName(name);
    pageRequest.setCode(code);
    pageRequest.setType(type);
    pageRequest.setEnabled(enabled);
    pageRequest.setCreateTime(createTime);
    return Ret.data(dataDomainService.pageDTOWithCondition(pageRequest));
  }

  /**
   * 创建数据域
   *
   * @param dataDomain 数据域实体
   * @return 创建结果
   */
  @PostMapping
  public Ret<Integer> create(@RequestBody SysDataDomain dataDomain) {
    try {
      return Ret.data(dataDomainService.create(dataDomain));
    } catch (IllegalArgumentException e) {
      return Ret.fail(400, e.getMessage());
    }
  }

  /**
   * 更新数据域
   *
   * @param id              数据域ID
   * @param updateDataDomainDTO 更新数据域信息DTO
   * @return 更新结果
   */
  @PutMapping("/{id}")
  public Ret<Integer> update(@PathVariable Long id, @RequestBody UpdateDataDomainDTO updateDataDomainDTO) {
    DataDomainResponseDTO dataDomainDTO = dataDomainService.getDTO(id);
    if (dataDomainDTO == null) {
      return Ret.fail(404, "data.domain.not.found");
    }
    try {
      return Ret.data(dataDomainService.updateDataDomainInfo(id, updateDataDomainDTO));
    } catch (IllegalArgumentException e) {
      return Ret.fail(400, e.getMessage());
    }
  }

  /**
   * 删除数据域
   *
   * @param id 数据域ID
   * @return 删除结果
   */
  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    DataDomainResponseDTO dataDomainDTO = dataDomainService.getDTO(id);
    if (dataDomainDTO == null) {
      return Ret.fail(404, "data.domain.not.found");
    }
    int result = dataDomainService.delete(id);
    return Ret.data(result);
  }
}

