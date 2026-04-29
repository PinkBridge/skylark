package cn.skylark.aiot_service.appint.dto;

import lombok.Data;

@Data
public class OutboundSubscriptionPageQuery {
  private Long channelId;
  private Integer pageNum;
  private Integer pageSize;
}
