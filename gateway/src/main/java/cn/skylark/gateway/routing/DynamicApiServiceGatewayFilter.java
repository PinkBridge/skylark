package cn.skylark.gateway.routing;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import reactor.core.publisher.Mono;

/**
 * Dynamic routing for business services in docker-compose without a registry.
 *
 * <p>Rule: /api/{service}/** -> http://{service}/** (container DNS name == service name)</p>
 *
 * <p>Skips paths handled by static gateway routes (see {@code application.yml}): those
 * services expect the full {@code /api/{service}/...} path on the downstream request.</p>
 */
@Component
public class DynamicApiServiceGatewayFilter implements GlobalFilter, Ordered {

    private static final Pattern API_SERVICE_PATTERN = Pattern.compile("^/api/([^/]+)(/.*)?$");

    @Override
    public int getOrder() {
        // Run before NettyRoutingFilter.
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();
        if (path == null || !path.startsWith("/api/")) {
            return chain.filter(exchange);
        }
        if (path.startsWith("/api/permission/")) {
            return chain.filter(exchange);
        }
        // business-api-service route: controllers use full /api/business-service/** prefix.
        if (path.startsWith("/api/business-service/")) {
            return chain.filter(exchange);
        }

        Matcher matcher = API_SERVICE_PATTERN.matcher(path);
        if (!matcher.matches()) {
            return chain.filter(exchange);
        }

        String service = matcher.group(1);
        String restPathGroup = matcher.group(2);
        final String restPath = (restPathGroup == null || restPathGroup.isEmpty()) ? "/" : restPathGroup;

        addOriginalRequestUrl(exchange, exchange.getRequest().getURI());

        URI originalUri = exchange.getRequest().getURI();
        URI routedUri = URI.create("http://" + service);
        // Preserve query string while rewriting path for downstream.
        URI requestUrl = URI.create(routedUri.toString() + restPath + (originalUri.getRawQuery() == null ? "" : "?" + originalUri.getRawQuery()));
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);

        ServerWebExchange mutated = exchange.mutate()
            .request(builder -> builder.path(restPath))
            .build();

        return chain.filter(mutated);
    }
}
