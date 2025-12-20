package QLDV.apigateway.filter;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import java.util.Map;


@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    // @Value("${auth.service.url}")
    // private String authServiceUrl;

    public JwtAuthGlobalFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // ✅ Bỏ qua Auth routes (login, validate, init-admin)
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // ✅ Bỏ qua các request OPTIONS (CORS)
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // ✅ Lấy token từ header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // ✅ Gọi auth-service validate token
        return webClientBuilder.build()
        .post()
        .uri(authServiceUrl + "/auth/validate-token")
        .contentType(MediaType.APPLICATION_JSON) // ✅ Gửi đúng dạng JSON
        .bodyValue(Map.of("token", token))       // ✅ Không phải chuỗi JSON thủ công
        .retrieve()
        .bodyToMono(Boolean.class)
        .flatMap(isValid -> {
            if (Boolean.TRUE.equals(isValid)) {
                System.out.println("✅ Token hợp lệ, cho phép request tiếp tục");
                return chain.filter(exchange);
            } else {
                System.out.println("❌ Token không hợp lệ");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        })
        .onErrorResume(e -> {
            System.out.println("🚫 Lỗi khi gọi validate-token: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        });
    }

    @Override
    public int getOrder() {
        return -1; // chạy sớm
    }
}
