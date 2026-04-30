package cn.skylark.aiot_service.iot.alarm.mapper;

import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmEvalStateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IotAlarmEvalStateMapper {
  int insert(IotAlarmEvalStateEntity entity);

  int update(IotAlarmEvalStateEntity entity);

  IotAlarmEvalStateEntity findByRuleAndDevice(@Param("tenantId") Long tenantId,
                                             @Param("ruleId") Long ruleId,
                                             @Param("productKey") String productKey,
                                             @Param("deviceKey") String deviceKey);

  int softDeleteById(@Param("id") Long id);
}

