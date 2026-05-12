package cn.skylark.aiot_service.iot.notification.mapper;

import cn.skylark.aiot_service.iot.notification.entity.IotNotificationChannelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotNotificationChannelMapper {
  int insert(IotNotificationChannelEntity entity);

  IotNotificationChannelEntity findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

  List<IotNotificationChannelEntity> listPage(@Param("tenantId") Long tenantId,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

  long countAll(@Param("tenantId") Long tenantId);

  int update(IotNotificationChannelEntity entity);

  int softDeleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

