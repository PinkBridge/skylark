package cn.skylark.web.exception;

import lombok.Data;

@Data
public class SkylarkErrorResponse {
  private int code;
  private Object data;
  private String message;

  public static SkylarkErrorResponse of(int code, String message) {
    SkylarkErrorResponse r = new SkylarkErrorResponse();
    r.setCode(code);
    r.setData(null);
    r.setMessage(message);
    return r;
  }
}

