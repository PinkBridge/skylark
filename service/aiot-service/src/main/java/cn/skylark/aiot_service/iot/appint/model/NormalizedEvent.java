package cn.skylark.aiot_service.iot.appint.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Canonical envelope published inside aiot-service and consumed by outbound integration.
 */
@Value
@Builder
public class NormalizedEvent {
  String eventId;
  String eventType;
  long occurredAt;
  Long tenantId;
  Long orgId;
  String source;
  Map<String, String> subject;
  Map<String, Object> data;
}
