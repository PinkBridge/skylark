package cn.skylark.aiot_service.iot.appint.dto;

import lombok.Data;

import java.util.List;

@Data
public class OutboundSubscriptionPageResponse {
  private List<OutboundSubscriptionResponse> records;
  private long total;
  private int pageNum;
  private int pageSize;
}
