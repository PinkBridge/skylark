package cn.skylark.aiot_service.iot.access.service;

import cn.skylark.aiot_service.iot.access.model.UpstreamIngestRequest;

public interface UpstreamIngestService {
  void ingest(UpstreamIngestRequest request);
}

