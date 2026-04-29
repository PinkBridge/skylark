package cn.skylark.aiot_service.iot.mgmt.model.dto.integration;

import lombok.Data;

import java.util.List;

@Data
public class OutboundChannelPageResponse {
  private List<OutboundChannelResponse> records;
  private long total;
  private int pageNum;
  private int pageSize;
}
