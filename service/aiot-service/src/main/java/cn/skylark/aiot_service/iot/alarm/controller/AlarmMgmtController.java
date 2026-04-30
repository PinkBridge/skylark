package cn.skylark.aiot_service.iot.alarm.controller;

import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordPageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordPageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRuleCreateRequest;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRulePageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRulePageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRuleResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRuleUpdateRequest;
import cn.skylark.aiot_service.iot.alarm.service.AlarmMgmtService;
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
public class AlarmMgmtController {
  private final AlarmMgmtService alarmMgmtService;

  public AlarmMgmtController(AlarmMgmtService alarmMgmtService) {
    this.alarmMgmtService = alarmMgmtService;
  }

  @GetMapping("/rules")
  public Ret<AlarmRulePageResponse> listRules(@ModelAttribute AlarmRulePageQuery query) {
    return Ret.data(alarmMgmtService.listRules(query));
  }

  @PostMapping("/rules")
  public Ret<AlarmRuleResponse> createRule(@Validated @RequestBody AlarmRuleCreateRequest request) {
    return Ret.data(alarmMgmtService.createRule(request));
  }

  @GetMapping("/rules/{id}")
  public Ret<AlarmRuleResponse> getRule(@PathVariable("id") Long id) {
    return Ret.data(alarmMgmtService.getRule(id));
  }

  @PutMapping("/rules/{id}")
  public Ret<AlarmRuleResponse> updateRule(@PathVariable("id") Long id, @Validated @RequestBody AlarmRuleUpdateRequest request) {
    return Ret.data(alarmMgmtService.updateRule(id, request));
  }

  @DeleteMapping("/rules/{id}")
  public Ret<Void> deleteRule(@PathVariable("id") Long id) {
    alarmMgmtService.deleteRule(id);
    return Ret.ok();
  }

  @GetMapping("/records")
  public Ret<AlarmRecordPageResponse> listRecords(@ModelAttribute AlarmRecordPageQuery query) {
    return Ret.data(alarmMgmtService.listRecords(query));
  }

  @GetMapping("/records/{id}")
  public Ret<AlarmRecordResponse> getRecord(@PathVariable("id") Long id) {
    return Ret.data(alarmMgmtService.getRecord(id));
  }
}

