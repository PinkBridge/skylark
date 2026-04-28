package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductPageResponse {
  private List<ProductResponse> records;
  private long total;
  private int pageNum;
  private int pageSize;
}

