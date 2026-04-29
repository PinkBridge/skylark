package cn.skylark.aiot_service.iot.appint.model;

/**
 * Normalized outbound event types (MVP).
 */
public final class IotIntegrationEventType {
  private IotIntegrationEventType() {}

  public static final String CONNECT_CONNECTED = "device.connect.connected";
  public static final String CONNECT_DISCONNECTED = "device.connect.disconnected";
  public static final String PROPERTY_REPORTED = "device.property.reported";
  public static final String EVENT_RAISED = "device.event.raised";
  public static final String SERVICE_INVOKE = "device.service.invoke";
  public static final String SERVICE_REPLY = "device.service.reply";
  public static final String MGMT_CREATED = "device.mgmt.created";
  public static final String MGMT_UPDATED = "device.mgmt.updated";
  public static final String MGMT_DELETED = "device.mgmt.deleted";

  public static String[] all() {
    return new String[] {
        CONNECT_CONNECTED,
        CONNECT_DISCONNECTED,
        PROPERTY_REPORTED,
        EVENT_RAISED,
        SERVICE_INVOKE,
        SERVICE_REPLY,
        MGMT_CREATED,
        MGMT_UPDATED,
        MGMT_DELETED
    };
  }
}
