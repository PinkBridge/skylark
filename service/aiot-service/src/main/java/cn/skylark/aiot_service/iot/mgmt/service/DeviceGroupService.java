package cn.skylark.aiot_service.iot.mgmt.service;

import cn.skylark.aiot_service.iot.mgmt.model.dto.AddDevicesToGroupRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.CreateDeviceGroupRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceGroupPageQuery;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceGroupPageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceGroupResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpdateDeviceGroupRequest;

import java.util.List;

public interface DeviceGroupService {
  DeviceGroupPageResponse list(DeviceGroupPageQuery query);

  DeviceGroupResponse create(CreateDeviceGroupRequest request);

  DeviceGroupResponse get(String groupKey);

  DeviceGroupResponse update(String groupKey, UpdateDeviceGroupRequest request);

  void delete(String groupKey);

  List<DeviceResponse> listGroupDevices(String groupKey);

  void addDevices(String groupKey, AddDevicesToGroupRequest request);

  void removeDevice(String groupKey, String productKey, String deviceKey);
}

