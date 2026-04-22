package cn.skylark.datadomain.starter.web;

import cn.skylark.datadomain.starter.SkylarkDataDomainProperties;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Reads tenant id from HTTP header into {@link cn.skylark.datadomain.starter.context.DataDomainContext}.
 */
@Slf4j
@RequiredArgsConstructor
public class TenantHeaderFilter extends OncePerRequestFilter {

  private final SkylarkDataDomainProperties props;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String raw = request.getHeader(props.getTenantHeader());
      if (StringUtils.hasText(raw)) {
        try {
          DataDomainContext.setTenantId(Long.parseLong(raw.trim()));
        } catch (NumberFormatException e) {
          log.warn("Invalid tenant header {}: {}", props.getTenantHeader(), raw);
        }
      }
      filterChain.doFilter(request, response);
    } finally {
      DataDomainContext.clear();
    }
  }
}
