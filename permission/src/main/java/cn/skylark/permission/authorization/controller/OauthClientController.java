package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.OauthClientPageRequest;
import cn.skylark.permission.authorization.dto.OauthClientResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateOauthClientDTO;
import cn.skylark.permission.authorization.entity.OauthClientDetails;
import cn.skylark.permission.authorization.service.OauthClientService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * OAuth2客户端控制器
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@RestController
@RequestMapping("/api/permission/apps")
public class OauthClientController {

  @Resource
  private OauthClientService oauthClientService;

  @GetMapping
  public Ret<List<OauthClientResponseDTO>> list() {
    return Ret.data(oauthClientService.listDTO());
  }

  /**
   * 分页查询客户端列表（支持搜索）
   *
   * @param page                 页码，从1开始，默认1
   * @param size                 每页大小，默认10
   * @param clientId             客户端ID（模糊搜索）
   * @param authorizedGrantTypes 授权模式（模糊搜索）
   * @param scope                作用域（模糊搜索）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<OauthClientResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String clientId,
      @RequestParam(required = false) String authorizedGrantTypes,
      @RequestParam(required = false) String scope) {
    OauthClientPageRequest pageRequest = new OauthClientPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setClientId(clientId);
    pageRequest.setAuthorizedGrantTypes(authorizedGrantTypes);
    pageRequest.setScope(scope);
    return Ret.data(oauthClientService.pageDTOWithCondition(pageRequest));
  }

  @GetMapping("/{clientId}")
  public Ret<OauthClientResponseDTO> get(@PathVariable String clientId) {
    OauthClientResponseDTO clientDTO = oauthClientService.getDTO(clientId);
    if (clientDTO == null) {
      return Ret.fail(404, "oauth.client.not.found");
    }
    return Ret.data(clientDTO);
  }

  @PostMapping
  public Ret<Integer> create(@RequestBody OauthClientDetails client) {
    return Ret.data(oauthClientService.create(client));
  }

  @PutMapping("/{clientId}")
  public Ret<Integer> update(@PathVariable String clientId, @RequestBody UpdateOauthClientDTO updateClientDTO) {
    OauthClientResponseDTO clientDTO = oauthClientService.getDTO(clientId);
    if (clientDTO == null) {
      return Ret.fail(404, "oauth.client.not.found");
    }
    return Ret.data(oauthClientService.updateClientInfo(clientId, updateClientDTO));
  }

  @DeleteMapping("/{clientId}")
  public Ret<Integer> delete(@PathVariable String clientId) {
    return Ret.data(oauthClientService.delete(clientId));
  }
}

