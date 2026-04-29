package cn.skylark.aiot_service.iot.appint.mapper;

import cn.skylark.aiot_service.iot.appint.model.OutboundDispatchRow;
import cn.skylark.aiot_service.iot.appint.entity.IotOutboundSubscriptionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotOutboundSubscriptionMapper {
  int insert(IotOutboundSubscriptionEntity entity);

  IotOutboundSubscriptionEntity findById(@Param("id") Long id);

  List<IotOutboundSubscriptionEntity> listByChannel(@Param("channelId") Long channelId,
                                                    @Param("offset") int offset,
                                                    @Param("pageSize") int pageSize);

  long countByChannel(@Param("channelId") Long channelId);

  List<OutboundDispatchRow> listDispatchRows(@Param("tenantId") Long tenantId);

  int update(IotOutboundSubscriptionEntity entity);

  int softDeleteById(@Param("id") Long id);
}
