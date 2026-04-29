package cn.skylark.aiot_service.iot.mgmt.controller;

import cn.skylark.aiot_service.iot.mgmt.model.dto.CreateDeviceConnectRecordRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.CreateDeviceRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceConnectRecordPageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceCurrentPropertyResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceEventRecordPageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DevicePropertyRecordPageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DevicePropertyRecordResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceRecordPageQuery;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceServiceRecordPageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductDataChannelResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpdateDeviceRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpdateProductDataChannelRequest;
import cn.skylark.aiot_service.iot.mgmt.service.DeviceService;
import cn.skylark.web.common.Ret;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/aiot-service/mgmt/products/{productKey}/devices")
public class DeviceController {

  private final DeviceService deviceService;

  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @PostMapping
  public Ret<DeviceResponse> create(@PathVariable("productKey") String productKey,
                               @Validated @RequestBody CreateDeviceRequest request) {
    return Ret.data(deviceService.create(productKey, request));
  }

  @GetMapping
  public Ret<List<DeviceResponse>> list(@PathVariable("productKey") String productKey) {
    return Ret.data(deviceService.list(productKey));
  }

  @GetMapping("/{deviceKey}")
  public Ret<DeviceResponse> get(@PathVariable("productKey") String productKey,
                            @PathVariable("deviceKey") String deviceKey) {
    return Ret.data(deviceService.get(productKey, deviceKey));
  }

  @PutMapping("/{deviceKey}")
  public Ret<DeviceResponse> update(@PathVariable("productKey") String productKey,
                               @PathVariable("deviceKey") String deviceKey,
                               @Validated @RequestBody UpdateDeviceRequest request) {
    return Ret.data(deviceService.update(productKey, deviceKey, request));
  }

  @PatchMapping("/{deviceKey}/enable")
  public Ret<DeviceResponse> enable(@PathVariable("productKey") String productKey,
                               @PathVariable("deviceKey") String deviceKey) {
    return Ret.data(deviceService.enable(productKey, deviceKey));
  }

  @PatchMapping("/{deviceKey}/disable")
  public Ret<DeviceResponse> disable(@PathVariable("productKey") String productKey,
                                @PathVariable("deviceKey") String deviceKey) {
    return Ret.data(deviceService.disable(productKey, deviceKey));
  }

  @PostMapping("/{deviceKey}/reset-secret")
  public Ret<DeviceResponse> resetSecret(@PathVariable("productKey") String productKey,
                                    @PathVariable("deviceKey") String deviceKey) {
    return Ret.data(deviceService.resetSecret(productKey, deviceKey));
  }

  @GetMapping("/{deviceKey}/data-channels")
  public Ret<List<ProductDataChannelResponse>> listDataChannels(@PathVariable("productKey") String productKey,
                                                           @PathVariable("deviceKey") String deviceKey) {
    return Ret.data(deviceService.listDataChannels(productKey, deviceKey));
  }

  @GetMapping("/{deviceKey}/current-properties")
  public Ret<List<DeviceCurrentPropertyResponse>> listCurrentProperties(@PathVariable("productKey") String productKey,
                                                                   @PathVariable("deviceKey") String deviceKey) {
    return Ret.data(deviceService.listCurrentProperties(productKey, deviceKey));
  }

  @PatchMapping("/{deviceKey}/data-channels/{id}")
  public Ret<Void> updateDataChannel(@PathVariable("productKey") String productKey,
                                @PathVariable("deviceKey") String deviceKey,
                                @PathVariable("id") Long id,
                                @Validated @RequestBody UpdateProductDataChannelRequest request) {
    deviceService.updateDataChannel(productKey, deviceKey, id, request);
    return Ret.ok();
  }

  @GetMapping("/{deviceKey}/property-records")
  public Ret<DevicePropertyRecordPageResponse> listPropertyRecords(@PathVariable("productKey") String productKey,
                                                              @PathVariable("deviceKey") String deviceKey,
                                                              @ModelAttribute DeviceRecordPageQuery query) {
    return Ret.data(deviceService.listPropertyRecords(productKey, deviceKey, query));
  }

  @GetMapping("/{deviceKey}/property-records/{propertyIdentifier}/latest")
  public Ret<DevicePropertyRecordResponse> getLatestPropertyRecord(@PathVariable("productKey") String productKey,
                                                              @PathVariable("deviceKey") String deviceKey,
                                                              @PathVariable("propertyIdentifier") String propertyIdentifier) {
    return Ret.data(deviceService.getLatestPropertyValue(productKey, deviceKey, propertyIdentifier));
  }

  @GetMapping("/{deviceKey}/event-records")
  public Ret<DeviceEventRecordPageResponse> listEventRecords(@PathVariable("productKey") String productKey,
                                                        @PathVariable("deviceKey") String deviceKey,
                                                        @ModelAttribute DeviceRecordPageQuery query) {
    return Ret.data(deviceService.listEventRecords(productKey, deviceKey, query));
  }

  @GetMapping("/{deviceKey}/service-records")
  public Ret<DeviceServiceRecordPageResponse> listServiceRecords(@PathVariable("productKey") String productKey,
                                                            @PathVariable("deviceKey") String deviceKey,
                                                            @ModelAttribute DeviceRecordPageQuery query) {
    return Ret.data(deviceService.listServiceRecords(productKey, deviceKey, query));
  }

  @GetMapping("/{deviceKey}/connect-records")
  public Ret<DeviceConnectRecordPageResponse> listConnectRecords(@PathVariable("productKey") String productKey,
                                                            @PathVariable("deviceKey") String deviceKey,
                                                            @ModelAttribute DeviceRecordPageQuery query) {
    return Ret.data(deviceService.listConnectRecords(productKey, deviceKey, query));
  }

  @PostMapping("/{deviceKey}/connect-records")
  public Ret<Void> createConnectRecord(@PathVariable("productKey") String productKey,
                                  @PathVariable("deviceKey") String deviceKey,
                                  @Validated @RequestBody CreateDeviceConnectRecordRequest request) {
    deviceService.createConnectRecord(productKey, deviceKey, request);
    return Ret.ok();
  }

  @DeleteMapping("/{deviceKey}")
  public Ret<Void> delete(@PathVariable("productKey") String productKey,
                     @PathVariable("deviceKey") String deviceKey) {
    deviceService.delete(productKey, deviceKey);
    return Ret.ok();
  }
}

