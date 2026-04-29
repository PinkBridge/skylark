package cn.skylark.aiot_service.iot.mgmt.controller;

import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceImportResult;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DevicePageQuery;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DevicePageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.DeviceResponse;
import cn.skylark.aiot_service.iot.mgmt.service.DeviceExcelImportService;
import cn.skylark.aiot_service.iot.mgmt.service.DeviceService;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import cn.skylark.web.common.Ret;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class DeviceGlobalController {
  private final DeviceService deviceService;
  private final DeviceExcelImportService deviceExcelImportService;

  public DeviceGlobalController(DeviceService deviceService, DeviceExcelImportService deviceExcelImportService) {
    this.deviceService = deviceService;
    this.deviceExcelImportService = deviceExcelImportService;
  }

  @GetMapping("/api/aiot-service/mgmt/devices")
  public Ret<List<DeviceResponse>> listAll() {
    return Ret.data(deviceService.listAll());
  }

  @GetMapping("/api/aiot-service/mgmt/devices/page")
  public Ret<DevicePageResponse> listPage(@ModelAttribute DevicePageQuery query) {
    return Ret.data(deviceService.listAllPage(query));
  }

  @GetMapping("/api/aiot-service/mgmt/devices/import-template")
  public ResponseEntity<byte[]> downloadDeviceImportTemplate() {
    try {
      byte[] bytes = deviceExcelImportService.buildTemplateXlsx();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
      headers.set(HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"device-import-template.xlsx\"");
      return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    } catch (IOException e) {
      throw new MgmtException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to build template");
    }
  }

  @PostMapping(value = "/api/aiot-service/mgmt/devices/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Ret<DeviceImportResult> importDevices(@RequestParam("file") MultipartFile file) {
    return Ret.data(deviceExcelImportService.importFromExcel(file));
  }
}

