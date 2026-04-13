package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.LoginLogPageRequest;
import cn.skylark.permission.authorization.dto.LoginLogResponseDTO;
import cn.skylark.permission.authorization.service.LoginLogService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志控制器
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@RestController
@RequestMapping("/api/permission/login-logs")
public class LoginLogController {

  @Resource
  private LoginLogService loginLogService;

  /**
   * 根据ID查询登录日志
   *
   * @param id 日志ID
   * @return 登录日志信息
   */
  @GetMapping("/{id}")
  public Ret<LoginLogResponseDTO> get(@PathVariable Long id) {
    LoginLogResponseDTO loginLogDTO = loginLogService.getDTO(id);
    if (loginLogDTO == null) {
      return Ret.fail(404, "login.log.not.found");
    }
    return Ret.data(loginLogDTO);
  }

  /**
   * 查询所有登录日志
   *
   * @return 登录日志列表
   */
  @GetMapping
  public Ret<List<LoginLogResponseDTO>> list() {
    return Ret.data(loginLogService.listDTO());
  }

  /**
   * 分页查询登录日志列表（支持搜索）
   *
   * @param page           页码，从1开始，默认1
   * @param size           每页大小，默认10
   * @param username       用户名（模糊搜索）
   * @param loginIp        登录IP（模糊搜索）
   * @param loginStatus    登录状态：SUCCESS-成功，FAILURE-失败
   * @param loginTimeStart 登录时间开始（查询此时间之后的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @param loginTimeEnd   登录时间结束（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @param tenantId       租户ID
   * @param clientId       应用ID（OAuth2客户端ID，模糊搜索）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<LoginLogResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String loginIp,
      @RequestParam(required = false) String loginStatus,
      @RequestParam(required = false) LocalDateTime loginTimeStart,
      @RequestParam(required = false) LocalDateTime loginTimeEnd,
      @RequestParam(required = false) Long tenantId,
      @RequestParam(required = false) String clientId) {
    LoginLogPageRequest pageRequest = new LoginLogPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setUsername(username);
    pageRequest.setLoginIp(loginIp);
    pageRequest.setLoginStatus(loginStatus);
    pageRequest.setLoginTimeStart(loginTimeStart);
    pageRequest.setLoginTimeEnd(loginTimeEnd);
    pageRequest.setTenantId(tenantId);
    pageRequest.setClientId(clientId);
    return Ret.data(loginLogService.pageDTOWithCondition(pageRequest));
  }

  /**
   * 删除登录日志
   *
   * @param id 日志ID
   * @return 删除结果
   */
  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    LoginLogResponseDTO loginLogDTO = loginLogService.getDTO(id);
    if (loginLogDTO == null) {
      return Ret.fail(404, "login.log.not.found");
    }
    int result = loginLogService.delete(id);
    return Ret.data(result);
  }
}

