package cn.skylark.permission.authentication.utils;

import cn.skylark.permission.utils.Ret;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * todo
 *
 * @author yaomianwei
 * @since 10:50 2025/11/17
 **/
public class AuthRetWriter {
  private AuthRetWriter() {
    throw new IllegalStateException("Utility class");
  }

  private static void ret(HttpServletResponse response, Integer code, Ret<Object> data) throws IOException {
    response.setStatus(code);
    response.setCharacterEncoding("utf-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    PrintWriter printWriter = response.getWriter();
    printWriter.print(JSON.toJSONString(data,
            SerializerFeature.WRITE_MAP_NULL_FEATURES,
            SerializerFeature.SortField));
    printWriter.flush();
    printWriter.close();
  }

  public static void retExpiredJwtUnauthorized(HttpServletResponse response) throws IOException {
    Ret<Object> fail = Ret.fail(HttpServletResponse.SC_UNAUTHORIZED, "expired.jwt.unauthorized");
    ret(response, HttpServletResponse.SC_UNAUTHORIZED, fail);
  }

  public static void retCheckJwtException(HttpServletResponse response) throws IOException {
    Ret<Object> fail = Ret.fail(HttpServletResponse.SC_UNAUTHORIZED, "check.jwt.exception");
    ret(response, HttpServletResponse.SC_UNAUTHORIZED, fail);
  }

  public static void retCheckTenantException(HttpServletResponse response) throws IOException {
    Ret<Object> fail = Ret.fail(HttpServletResponse.SC_UNAUTHORIZED, "tenant.available.exception");
    ret(response, HttpServletResponse.SC_UNAUTHORIZED, fail);
  }
}
