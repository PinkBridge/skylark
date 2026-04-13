package cn.skylark.permission.authentication.filter;

import cn.skylark.permission.authentication.utils.AuthRetWriter;
import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.TenantResponseDTO;
import cn.skylark.permission.authorization.entity.SysTenant;
import cn.skylark.permission.authorization.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 租户过滤器
 * 从请求头 X-Tenant-Id 或域名提取租户ID
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

  /**
   * 租户ID请求头名称
   */
  private static final String TENANT_ID_HEADER = "X-Tenant-Id";

  @Resource
  private TenantService tenantService;

  /**
   * 从请求头提取租户ID
   *
   * @param request HTTP请求
   * @return 租户ID，如果不存在则返回null
   */
  private Long extractTenantIdFromHeader(HttpServletRequest request) {
    String tenantIdStr = request.getHeader(TENANT_ID_HEADER);
    if (StringUtils.hasText(tenantIdStr)) {
      try {
        return Long.parseLong(tenantIdStr);
      } catch (NumberFormatException e) {
        log.warn("Invalid tenant ID format in header {}: {}", TENANT_ID_HEADER, tenantIdStr);
        return null;
      }
    }
    return null;
  }

  /**
   * 从域名提取租户ID
   *
   * @param request HTTP请求
   * @return 租户ID，如果不存在则返回null
   */
  private Long extractTenantIdFromDomain(HttpServletRequest request) {
    String host = request.getHeader("Host");
    if (!StringUtils.hasText(host)) {
      return null;
    }

    // 移除端口号（如果有）
    String domain = host.split(":")[0];

    // 查询租户信息
    TenantResponseDTO dtoByDomain = tenantService.getDTOByDomain(domain);
    if (dtoByDomain != null) {
      return dtoByDomain.getId();
    }

    // 如果直接匹配失败，尝试匹配子域名
    // 例如：www.bjtech.com 匹配 bjtech.com
    if (domain.startsWith("www.")) {
      String domainWithoutWww = domain.substring(4);
      dtoByDomain = tenantService.getDTOByDomain(domainWithoutWww);
      if (dtoByDomain != null) {
        return dtoByDomain.getId();
      }
    }

    return null;
  }

  private boolean tenantAvailable(Long tenantId) {
    SysTenant sysTenant = tenantService.get(tenantId);
    return sysTenant != null
            && "ACTIVE".equals(sysTenant.getStatus())
            && LocalDateTime.now().isBefore(sysTenant.getExpireTime());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    try {
      // 优先从请求头获取租户ID
      Long tenantId = extractTenantIdFromHeader(request);

      // 如果请求头没有，则从域名获取
      if (tenantId == null) {
        tenantId = extractTenantIdFromDomain(request);
      }

      // 设置租户上下文
      if (tenantId != null) {
        if (!tenantAvailable(tenantId)) {
          AuthRetWriter.retCheckTenantException(response);
        }
        TenantContext.setTenantId(tenantId);
        log.debug("Tenant ID set from request: {}", tenantId);
      } else {
        log.debug("No tenant ID found in request header or domain: {}", request.getRequestURI());
      }

      filterChain.doFilter(request, response);
    } finally {
      // 请求结束后清除租户上下文，防止内存泄漏
      TenantContext.clear();
    }
  }

  /**
   * 排除不需要租户过滤的路径
   * 例如：OAuth相关接口、租户查询接口等
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // 排除OAuth相关接口
    if (path.startsWith("/oauth/")) {
      return true;
    }
    // 排除租户查询接口（根据域名查询租户）
    if (path.startsWith("/api/permission/tenants/domain/")) {
      return true;
    }
    return false;
  }
}

