package cn.skylark.aiot_service.iot.mgmt.controller;

import cn.skylark.aiot_service.iot.mgmt.model.dto.ThingModelResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpsertThingModelRequest;
import cn.skylark.aiot_service.iot.mgmt.service.ThingModelService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aiot-service/mgmt/products/{productKey}/thing-model")
public class ThingModelController {

  private final ThingModelService thingModelService;

  public ThingModelController(ThingModelService thingModelService) {
    this.thingModelService = thingModelService;
  }

  @PutMapping
  public ThingModelResponse upsert(@PathVariable("productKey") String productKey,
                                   @Validated @RequestBody UpsertThingModelRequest request) {
    return thingModelService.upsert(productKey, request);
  }

  @GetMapping
  public ThingModelResponse get(@PathVariable("productKey") String productKey) {
    return thingModelService.get(productKey);
  }
}

