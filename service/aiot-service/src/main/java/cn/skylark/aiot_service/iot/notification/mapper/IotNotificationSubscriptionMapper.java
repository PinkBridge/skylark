package cn.skylark.aiot_service.iot.notification.mapper;

import cn.skylark.aiot_service.iot.notification.entity.IotNotificationSubscriptionEntity;
import cn.skylark.aiot_service.iot.notification.model.NotificationDispatchRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotNotificationSubscriptionMapper {
  int insert(IotNotificationSubscriptionEntity entity);

  IotNotificationSubscriptionEntity findById(@Param("tenantId") Long tenantId, @Param("id") Long id);

  List<IotNotificationSubscriptionEntity> listPage(@Param("tenantId") Long tenantId,
                                                   @Param("channelId") Long channelId,
                                                   @Param("deviceGroupKey") String deviceGroupKey,
                                                   @Param("offset") int offset,
                                                   @Param("pageSize") int pageSize);

  long countPage(@Param("tenantId") Long tenantId,
                 @Param("channelId") Long channelId,
                 @Param("deviceGroupKey") String deviceGroupKey);

  /**
   * Join channel + enabled subscriptions by group keys for dispatch.
   */
  List<NotificationDispatchRow> listDispatchRowsByGroupKeys(@Param("tenantId") Long tenantId,
                                                           @Param("groupKeys") List<String> groupKeys);

  int update(IotNotificationSubscriptionEntity entity);

  int softDeleteById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}

