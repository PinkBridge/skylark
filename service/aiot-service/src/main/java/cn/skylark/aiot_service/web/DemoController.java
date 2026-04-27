package cn.skylark.aiot_service.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/business-service/demo")
public class DemoController {

  @GetMapping("/ping")
  public Map<String, Object> ping(@RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("ok", true);
    out.put("tenantId", tenantId);
    return out;
  }
}

