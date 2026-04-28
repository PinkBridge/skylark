package cn.skylark.aiot_service.iot.access.controller;

import cn.skylark.aiot_service.iot.access.model.EmqxAclRequest;
import cn.skylark.aiot_service.iot.access.model.EmqxAuthRequest;
import cn.skylark.aiot_service.iot.access.model.EmqxAuthResponse;
import cn.skylark.aiot_service.iot.access.model.EmqxAuthzResponse;
import cn.skylark.aiot_service.iot.access.service.DeviceAccessAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aiot-service/access/emqx")
public class EmqxAuthController {

  private final DeviceAccessAuthService authService;

  public EmqxAuthController(DeviceAccessAuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/auth")
  public ResponseEntity<EmqxAuthResponse> auth(@RequestBody(required = false) EmqxAuthRequest body,
                                               @RequestParam(value = "username", required = false) String qUsername,
                                               @RequestParam(value = "password", required = false) String qPassword) {
    String username = body != null ? body.getUsername() : qUsername;
    String password = body != null ? body.getPassword() : qPassword;
    boolean ok = authService.authenticate(username, password);
    return ResponseEntity.ok(ok ? EmqxAuthResponse.allow() : EmqxAuthResponse.deny());
  }

  @PostMapping("/acl")
  public ResponseEntity<EmqxAuthzResponse> acl(@RequestBody(required = false) EmqxAclRequest body,
                                               @RequestParam(value = "username", required = false) String qUsername,
                                               @RequestParam(value = "action", required = false) String qAction,
                                               @RequestParam(value = "topic", required = false) String qTopic) {
    String username = body != null ? body.getUsername() : qUsername;
    String action = body != null ? body.getAction() : qAction;
    String topic = body != null ? body.getTopic() : qTopic;
    boolean ok = authService.allowAcl(username, action, topic);
    return ResponseEntity.ok(ok ? EmqxAuthzResponse.allow() : EmqxAuthzResponse.deny());
  }
}

