package cn.skylark.aiot_service.appint.mapper;

import cn.skylark.aiot_service.appint.entity.IotOutboundChannelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IotOutboundChannelMapper {
  int insert(IotOutboundChannelEntity entity);

  IotOutboundChannelEntity findById(@Param("id") Long id);

  List<IotOutboundChannelEntity> listPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

  long countAll();

  int update(IotOutboundChannelEntity entity);

  int softDeleteById(@Param("id") Long id);
}
