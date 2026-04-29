package cn.skylark.aiot_service.appint.dto;

import lombok.Data;

import java.util.List;

@Data
public class OutboundChannelPageResponse {
  private List<OutboundChannelResponse> records;
  private long total;
  private int pageNum;
  private int pageSize;
}
