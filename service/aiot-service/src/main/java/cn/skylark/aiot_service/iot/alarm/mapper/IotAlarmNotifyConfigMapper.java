package cn.skylark.aiot_service.iot.alarm.mapper;

import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmNotifyConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotAlarmNotifyConfigMapper {
  int insert(IotAlarmNotifyConfigEntity entity);

  int update(IotAlarmNotifyConfigEntity entity);

  int softDeleteById(@Param("id") Long id);

  IotAlarmNotifyConfigEntity findById(@Param("id") Long id);

  List<IotAlarmNotifyConfigEntity> listByTenantAndRule(@Param("tenantId") Long tenantId,
                                                       @Param("ruleId") Long ruleId,
                                                       @Param("enabledOnly") Integer enabledOnly);

  List<IotAlarmNotifyConfigEntity> listPage(@Param("tenantId") Long tenantId,
                                          @Param("ruleId") Long ruleId,
                                          @Param("offset") int offset,
                                          @Param("pageSize") int pageSize);

  long countPage(@Param("tenantId") Long tenantId, @Param("ruleId") Long ruleId);
}
