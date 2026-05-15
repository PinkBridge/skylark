package cn.skylark.aiot_service.iot.notify.mapper;

import cn.skylark.aiot_service.iot.notify.entity.IotNotifyChannelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotNotifyChannelMapper {
  int insert(IotNotifyChannelEntity entity);

  int update(IotNotifyChannelEntity entity);

  int softDeleteById(@Param("id") Long id);

  IotNotifyChannelEntity findById(@Param("id") Long id);

  List<IotNotifyChannelEntity> listByTenantAndKind(@Param("tenantId") Long tenantId,
                                                   @Param("channelKind") String channelKind,
                                                   @Param("enabledOnly") Integer enabledOnly);

  List<IotNotifyChannelEntity> listPage(@Param("tenantId") Long tenantId,
                                        @Param("channelKind") String channelKind,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

  long countPage(@Param("tenantId") Long tenantId, @Param("channelKind") String channelKind);
}
