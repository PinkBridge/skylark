package cn.skylark.aiot_service.iot.alarm.mapper;

import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotAlarmRuleMapper {
  int insert(IotAlarmRuleEntity entity);

  int update(IotAlarmRuleEntity entity);

  IotAlarmRuleEntity findById(@Param("id") Long id);

  List<IotAlarmRuleEntity> listPage(@Param("deviceGroupKey") String deviceGroupKey,
                                    @Param("enabled") Integer enabled,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);

  long countPage(@Param("deviceGroupKey") String deviceGroupKey,
                 @Param("enabled") Integer enabled);

  List<IotAlarmRuleEntity> listEnabledByGroupKeys(@Param("tenantId") Long tenantId,
                                                  @Param("groupKeys") List<String> groupKeys);

  int softDeleteById(@Param("id") Long id);
}

