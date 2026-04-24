package cn.skylark.web.common;

import lombok.Data;

@Data
public class Ret<T> {
  private int code;
  private T data;
  private String message;

  public static <T> Ret<T> data(T data) {
    Ret<T> ret = new Ret<>();
    ret.setCode(200);
    ret.setData(data);
    ret.setMessage("success");
    return ret;
  }

  public static <T> Ret<T> ok() {
    Ret<T> ret = new Ret<>();
    ret.setCode(200);
    ret.setData(null);
    ret.setMessage("success");
    return ret;
  }

  public static <T> Ret<T> fail(int code, String message) {
    Ret<T> ret = new Ret<>();
    ret.setCode(code);
    ret.setData(null);
    ret.setMessage(message);
    return ret;
  }
}

