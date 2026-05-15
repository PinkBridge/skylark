package cn.skylark.aiot_service.iot.alarm.mapper;

import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmNotifyDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotAlarmNotifyDeliveryMapper {
  int insert(IotAlarmNotifyDeliveryEntity entity);

  List<IotAlarmNotifyDeliveryEntity> listPage(@Param("tenantId") Long tenantId,
                                              @Param("ruleId") Long ruleId,
                                              @Param("notifyConfigId") Long notifyConfigId,
                                              @Param("channel") String channel,
                                              @Param("status") String status,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

  long countPage(@Param("tenantId") Long tenantId,
                 @Param("ruleId") Long ruleId,
                 @Param("notifyConfigId") Long notifyConfigId,
                 @Param("channel") String channel,
                 @Param("status") String status);
}
