package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.OrganizationPageRequest;
import cn.skylark.permission.authorization.dto.OrganizationResponseDTO;
import cn.skylark.permission.authorization.dto.OrganizationTreeNode;
import cn.skylark.permission.authorization.dto.UpdateOrganizationDTO;
import cn.skylark.permission.authorization.entity.SysOrganization;
import cn.skylark.permission.authorization.service.OrganizationService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织控制器
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@RestController
@RequestMapping("/api/permission/orgs")
public class OrganizationController {

  @Resource
  private OrganizationService organizationService;

  @GetMapping
  public Ret<List<OrganizationResponseDTO>> list() {
    return Ret.data(organizationService.listDTO());
  }

  /**
   * 获取组织树
   *
   * @param name 组织名称（模糊搜索，可选）
   * @return 组织树
   */
  @GetMapping("/tree")
  public Ret<List<OrganizationTreeNode>> tree(@RequestParam(required = false) String name) {
    return Ret.data(organizationService.organizationTree(name));
  }

  /**
   * 分页查询组织列表（支持搜索）
   *
   * @param page       页码，从1开始，默认1
   * @param size       每页大小，默认10
   * @param name       组织名称（模糊搜索）
   * @param code       组织编码（模糊搜索）
   * @param phone      联系电话（模糊搜索）
   * @param email      邮箱（模糊搜索）
   * @param type       组织类型（精确搜索）
   * @param status     状态（精确搜索）
   * @param createTime 创建时间（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<OrganizationResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDateTime createTime) {
    OrganizationPageRequest pageRequest = new OrganizationPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setName(name);
    pageRequest.setCode(code);
    pageRequest.setPhone(phone);
    pageRequest.setEmail(email);
    pageRequest.setType(type);
    pageRequest.setStatus(status);
    pageRequest.setCreateTime(createTime);
    return Ret.data(organizationService.pageDTOWithCondition(pageRequest));
  }

  @GetMapping("/{id}")
  public Ret<OrganizationResponseDTO> get(@PathVariable Long id) {
    OrganizationResponseDTO organizationDTO = organizationService.getDTO(id);
    if (organizationDTO == null) {
      return Ret.fail(404, "organization.not.found");
    }
    return Ret.data(organizationDTO);
  }

  @PostMapping
  public Ret<Integer> create(@RequestBody SysOrganization organization) {
    return Ret.data(organizationService.create(organization));
  }

  @PutMapping("/{id}")
  public Ret<Integer> update(@PathVariable Long id, @RequestBody UpdateOrganizationDTO updateOrganizationDTO) {
    OrganizationResponseDTO organizationDTO = organizationService.getDTO(id);
    if (organizationDTO == null) {
      return Ret.fail(404, "organization.not.found");
    }
    return Ret.data(organizationService.updateOrganizationInfo(id, updateOrganizationDTO));
  }

  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    return Ret.data(organizationService.delete(id));
  }
}

