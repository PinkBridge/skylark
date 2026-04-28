package cn.skylark.aiot_service.iot.mgmt.controller;

import cn.skylark.aiot_service.iot.mgmt.model.dto.DevicePageQuery;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DevicePageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceResponse;
import cn.skylark.aiot_service.iot.mgmt.service.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeviceGlobalController {
  private final DeviceService deviceService;

  public DeviceGlobalController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @GetMapping("/api/aiot-service/mgmt/devices")
  public List<DeviceResponse> listAll() {
    return deviceService.listAll();
  }

  @GetMapping("/api/aiot-service/mgmt/devices/page")
  public DevicePageResponse listPage(@ModelAttribute DevicePageQuery query) {
    return deviceService.listAllPage(query);
  }
}

