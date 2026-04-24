package cn.skylark.web.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
  private List<T> records;
  private Long total;
  private Integer page;
  private Integer size;
  private Integer pages;

  public PageResult() {
  }

  public PageResult(List<T> records, Long total, Integer page, Integer size) {
    this.records = records;
    this.total = total;
    this.page = page;
    this.size = size;
    this.pages = (int) Math.ceil((double) total / size);
  }
}

