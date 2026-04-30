package cn.skylark.aiot_service.iot.alarm.mapper;

import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotAlarmMapper {
  int insert(IotAlarmEntity entity);

  int update(IotAlarmEntity entity);

  IotAlarmEntity findById(@Param("id") Long id);

  IotAlarmEntity findActiveByRuleAndDevice(@Param("tenantId") Long tenantId,
                                          @Param("ruleId") Long ruleId,
                                          @Param("productKey") String productKey,
                                          @Param("deviceKey") String deviceKey);

  List<IotAlarmEntity> listPage(@Param("deviceGroupKey") String deviceGroupKey,
                                @Param("ruleId") Long ruleId,
                                @Param("severity") String severity,
                                @Param("status") String status,
                                @Param("offset") int offset,
                                @Param("pageSize") int pageSize);

  long countPage(@Param("deviceGroupKey") String deviceGroupKey,
                 @Param("ruleId") Long ruleId,
                 @Param("severity") String severity,
                 @Param("status") String status);

  int softDeleteById(@Param("id") Long id);
}

