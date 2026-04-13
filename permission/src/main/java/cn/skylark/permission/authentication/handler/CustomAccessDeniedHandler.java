package cn.skylark.permission.authentication.handler;

import cn.skylark.permission.utils.Ret;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author yaomianwei
 * @since 11:55 2025/11/7
 **/
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
    Ret<Object> fail = Ret.fail(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
