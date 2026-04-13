package cn.skylark.permission.authentication.service;

import cn.skylark.permission.utils.Ret;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author yaomianwei
 * @since 12:01 2025/11/7
 **/
@Component
public class CustomAuthenticationEntryPointImpl implements AuthenticationEntryPoint {
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException, ServletException {
    Ret<Object> fail = Ret.fail(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding("utf-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    PrintWriter printWriter = response.getWriter();
    printWriter.print(JSON.toJSONString(fail,
            SerializerFeature.WRITE_MAP_NULL_FEATURES,
            SerializerFeature.SortField));
    printWriter.flush();
    printWriter.close();
  }
}
