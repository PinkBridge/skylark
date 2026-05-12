package cn.skylark.aiot_service.iot.access.service;

import cn.skylark.aiot_service.iot.access.model.DownstreamPublishRequest;

import java.util.Optional;

public interface EmqxManagementClient {
  Optional<String> publish(DownstreamPublishRequest req);
}

