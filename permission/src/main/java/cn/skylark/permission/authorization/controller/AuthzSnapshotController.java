package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.RbacSnapshotResponse;
import cn.skylark.permission.authorization.service.AuthzSnapshotService;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/permission/authz")
public class AuthzSnapshotController {

  @Resource
  private AuthzSnapshotService service;

  @GetMapping("/snapshot")
  public Ret<RbacSnapshotResponse> snapshot(@RequestParam String appCode,
                                           @RequestParam(required = false, defaultValue = "0") long sinceVersion) {
    return Ret.data(service.snapshot(appCode, sinceVersion));
  }
}

