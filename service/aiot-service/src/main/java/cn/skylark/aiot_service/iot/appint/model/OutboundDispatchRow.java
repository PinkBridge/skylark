package cn.skylark.aiot_service.iot.appint.model;

import lombok.Data;

@Data
public class OutboundDispatchRow {
  private Long subscriptionId;
  private Long subscriptionOrgId;
  private String eventTypes;
  private String filterJson;
  private String transformJson;
  private Long channelId;
  private String channelType;
  private String channelConfigJson;
}
