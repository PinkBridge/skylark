package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.PlatformInitStateDTO;
import cn.skylark.permission.authorization.dto.PlatformInitializeRequestDTO;
import cn.skylark.permission.authorization.service.PlatformInitService;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/permission/platform-init")
public class PlatformInitController {

  @Resource
  private PlatformInitService platformInitService;

  @GetMapping("/state")
  public Ret<PlatformInitStateDTO> state() {
    return Ret.data(platformInitService.state());
  }

  @PostMapping("/initialize")
  public Ret<Void> initialize(@RequestBody PlatformInitializeRequestDTO body) {
    try {
      platformInitService.initialize(body);
      return Ret.ok();
    } catch (IllegalArgumentException e) {
      return Ret.fail(400, e.getMessage());
    } catch (IllegalStateException e) {
      // initialized already or missing default rows
      return Ret.fail(409, e.getMessage());
    }
  }
}

