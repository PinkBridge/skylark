package cn.skylark.aiot_service.iot.mgmt.controller;

import cn.skylark.aiot_service.iot.mgmt.model.dto.CopyProductRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.CreateProductRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductDataChannelResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductPageQuery;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductPageResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.ProductResponse;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpdateProductDataChannelRequest;
import cn.skylark.aiot_service.iot.mgmt.model.dto.UpdateProductRequest;
import cn.skylark.aiot_service.iot.mgmt.service.ProductService;
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
@RequestMapping("/api/aiot-service/mgmt/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  public Ret<ProductResponse> create(@Validated @RequestBody CreateProductRequest request) {
    return Ret.data(productService.create(request));
  }

  @GetMapping
  public Ret<ProductPageResponse> list(@ModelAttribute ProductPageQuery query) {
    return Ret.data(productService.list(query));
  }

  @GetMapping("/{productKey}")
  public Ret<ProductResponse> get(@PathVariable("productKey") String productKey) {
    return Ret.data(productService.get(productKey));
  }

  @PutMapping("/{productKey}")
  public Ret<ProductResponse> update(@PathVariable("productKey") String productKey,
                                @Validated @RequestBody UpdateProductRequest request) {
    return Ret.data(productService.update(productKey, request));
  }

  @PatchMapping("/{productKey}/enable")
  public Ret<ProductResponse> enable(@PathVariable("productKey") String productKey) {
    return Ret.data(productService.enable(productKey));
  }

  @PatchMapping("/{productKey}/disable")
  public Ret<ProductResponse> disable(@PathVariable("productKey") String productKey) {
    return Ret.data(productService.disable(productKey));
  }

  @PostMapping("/{productKey}/copy")
  public Ret<ProductResponse> copy(@PathVariable("productKey") String productKey,
                              @Validated @RequestBody CopyProductRequest request) {
    return Ret.data(productService.copy(productKey, request));
  }

  @GetMapping("/{productKey}/data-channels")
  public Ret<List<ProductDataChannelResponse>> listDataChannels(@PathVariable("productKey") String productKey) {
    return Ret.data(productService.listDataChannels(productKey));
  }

  @PatchMapping("/{productKey}/data-channels/{id}")
  public Ret<Void> updateDataChannel(@PathVariable("productKey") String productKey,
                                @PathVariable("id") Long id,
                                @Validated @RequestBody UpdateProductDataChannelRequest request) {
    productService.updateDataChannel(productKey, id, request);
    return Ret.ok();
  }

  @DeleteMapping("/{productKey}")
  public Ret<Void> delete(@PathVariable("productKey") String productKey) {
    productService.delete(productKey);
    return Ret.ok();
  }
}

