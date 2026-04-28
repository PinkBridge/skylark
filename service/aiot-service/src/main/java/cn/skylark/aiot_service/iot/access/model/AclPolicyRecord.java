package cn.skylark.aiot_service.iot.access.model;

import lombok.Data;

@Data
public class AclPolicyRecord {
  private Long id;
  private Long tenantId;
  private String productKey;
  private String subjectType;
  private String subjectValue;
  private String action;
  private String topicPattern;
  private String effect;
  private Integer priority;
  private Integer enabled;
}

