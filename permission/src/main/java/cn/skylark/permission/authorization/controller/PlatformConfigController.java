package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.PlatformConfigItemDTO;
import cn.skylark.permission.authorization.dto.PlatformConfigValueUpdateDTO;
import cn.skylark.permission.authorization.service.PlatformConfigService;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/permission/platform-configs")
public class PlatformConfigController {

  @Resource
  private PlatformConfigService platformConfigService;

  @GetMapping
  public Ret<List<PlatformConfigItemDTO>> list() {
    return Ret.data(platformConfigService.listAll());
  }

  @PutMapping("/{configKey:.+}")
  public Ret<Void> update(@PathVariable("configKey") String configKey,
                          @RequestBody PlatformConfigValueUpdateDTO body) {
    if (body == null || body.getConfigValue() == null) {
      return Ret.fail(400, "platform.config.value.required");
    }
    int n = platformConfigService.updateValue(configKey, body.getConfigValue());
    if (n <= 0) {
      return Ret.fail(404, "platform.config.not.found");
    }
    return Ret.ok();
  }
}
