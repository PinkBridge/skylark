package cn.skylark.aiot_service.iot.mgmt.mapper;

import cn.skylark.aiot_service.iot.mgmt.model.entity.IotOutboundDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotOutboundDeliveryMapper {
  int insert(IotOutboundDeliveryEntity entity);

  int update(IotOutboundDeliveryEntity entity);

  IotOutboundDeliveryEntity findById(@Param("id") Long id);

  List<IotOutboundDeliveryEntity> listPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

  long countAll();

  List<IotOutboundDeliveryEntity> listDueRetry(@Param("limit") int limit, @Param("maxAttempts") int maxAttempts);
}
