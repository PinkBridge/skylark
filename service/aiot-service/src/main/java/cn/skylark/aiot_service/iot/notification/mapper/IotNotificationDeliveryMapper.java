package cn.skylark.aiot_service.iot.notification.mapper;

import cn.skylark.aiot_service.iot.notification.entity.IotNotificationDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotNotificationDeliveryMapper {
  int insert(IotNotificationDeliveryEntity entity);

  IotNotificationDeliveryEntity findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

  List<IotNotificationDeliveryEntity> listPage(@Param("tenantId") Long tenantId,
                                               @Param("status") String status,
                                               @Param("channelId") Long channelId,
                                               @Param("subscriptionId") Long subscriptionId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

  long countPage(@Param("tenantId") Long tenantId,
                 @Param("status") String status,
                 @Param("channelId") Long channelId,
                 @Param("subscriptionId") Long subscriptionId);

  List<IotNotificationDeliveryEntity> listDueRetry(@Param("limit") int limit,
                                                   @Param("maxAttempts") int maxAttempts);

  int update(IotNotificationDeliveryEntity entity);
}

