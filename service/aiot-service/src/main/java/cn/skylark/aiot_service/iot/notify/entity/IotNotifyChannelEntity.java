package cn.skylark.aiot_service.iot.notify.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotNotifyChannelEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  /** EMAIL | SMS */
  private String channelKind;
  /** SMTP | ALIYUN | TWILIO | AWS_SNS */
  private String provider;
  private String name;
  private Integer enabled;
  private String configJson;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
