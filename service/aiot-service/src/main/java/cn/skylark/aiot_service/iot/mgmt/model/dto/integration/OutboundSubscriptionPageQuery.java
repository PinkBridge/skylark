package cn.skylark.aiot_service.iot.mgmt.model.dto.integration;

import lombok.Data;

@Data
public class OutboundSubscriptionPageQuery {
  private Long channelId;
  private Integer pageNum;
  private Integer pageSize;
}
