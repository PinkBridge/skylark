package cn.skylark.aiot_service.iot.alarm.controller;

import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigCreateRequest;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigPageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigPageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigUpdateRequest;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyDeliveryPageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyDeliveryPageResponse;
import cn.skylark.aiot_service.iot.alarm.service.AlarmNotifyMgmtService;
import cn.skylark.web.common.Ret;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aiot-service/alarm")
public class AlarmNotifyMgmtController {
  private final AlarmNotifyMgmtService alarmNotifyMgmtService;

  public AlarmNotifyMgmtController(AlarmNotifyMgmtService alarmNotifyMgmtService) {
    this.alarmNotifyMgmtService = alarmNotifyMgmtService;
  }

  @GetMapping("/notify-configs")
  public Ret<AlarmNotifyConfigPageResponse> listNotifyConfigs(@ModelAttribute AlarmNotifyConfigPageQuery query) {
    return Ret.data(alarmNotifyMgmtService.listNotifyConfigs(query));
  }

  @PostMapping("/notify-configs")
  public Ret<AlarmNotifyConfigResponse> createNotifyConfig(@Validated @RequestBody AlarmNotifyConfigCreateRequest request) {
    return Ret.data(alarmNotifyMgmtService.createNotifyConfig(request));
  }

  @GetMapping("/notify-configs/{id}")
  public Ret<AlarmNotifyConfigResponse> getNotifyConfig(@PathVariable("id") Long id) {
    return Ret.data(alarmNotifyMgmtService.getNotifyConfig(id));
  }

  @PutMapping("/notify-configs/{id}")
  public Ret<AlarmNotifyConfigResponse> updateNotifyConfig(@PathVariable("id") Long id,
                                                         @Validated @RequestBody AlarmNotifyConfigUpdateRequest request) {
    return Ret.data(alarmNotifyMgmtService.updateNotifyConfig(id, request));
  }

  @DeleteMapping("/notify-configs/{id}")
  public Ret<Void> deleteNotifyConfig(@PathVariable("id") Long id) {
    alarmNotifyMgmtService.deleteNotifyConfig(id);
    return Ret.ok();
  }

  @PostMapping("/notify-configs/{id}/test")
  public Ret<Void> testNotifyConfig(@PathVariable("id") Long id) {
    alarmNotifyMgmtService.testNotifyConfig(id);
    return Ret.ok();
  }

  @GetMapping("/notify-deliveries")
  public Ret<AlarmNotifyDeliveryPageResponse> listDeliveries(@ModelAttribute AlarmNotifyDeliveryPageQuery query) {
    return Ret.data(alarmNotifyMgmtService.listDeliveries(query));
  }
}
