package cn.skylark.aiot_service.iot.notify.dto;

import lombok.Data;

@Data
public class NotifyChannelPageQuery {
  private Integer pageNum;
  private Integer pageSize;
  /** EMAIL or SMS or empty for all */
  private String channelKind;
}
