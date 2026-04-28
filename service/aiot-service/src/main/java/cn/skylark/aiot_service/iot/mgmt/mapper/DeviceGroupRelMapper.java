package cn.skylark.aiot_service.iot.mgmt.mapper;

import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceGroupMemberCount;
import cn.skylark.aiot_service.iot.mgmt.model.entity.DeviceGroupRelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceGroupRelMapper {
  int insert(DeviceGroupRelEntity entity);

  int delete(@Param("groupKey") String groupKey,
             @Param("productKey") String productKey,
             @Param("deviceKey") String deviceKey);

  int restore(@Param("groupKey") String groupKey,
              @Param("productKey") String productKey,
              @Param("deviceKey") String deviceKey);

  int deleteByGroupKey(@Param("groupKey") String groupKey);

  int deleteByProductKey(@Param("productKey") String productKey);

  int deleteByProductAndDevice(@Param("productKey") String productKey,
                               @Param("deviceKey") String deviceKey);

  List<DeviceGroupRelEntity> listByGroupKey(@Param("groupKey") String groupKey);

  long countByGroupKey(@Param("groupKey") String groupKey);

  List<DeviceGroupMemberCount> countMembersByGroupKeys(@Param("groupKeys") List<String> groupKeys);
}

