package cn.skylark.aiot_service.iot.access.mapper;

import cn.skylark.aiot_service.iot.access.model.AccessDeviceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccessDeviceMapper {
  List<AccessDeviceRecord> findByDeviceKey(@Param("deviceKey") String deviceKey);
}

