package com.zaror.gateway.security;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.JwtException;
import reactor.core.publisher.Mono;

// Se ejecuta en TODAS las peticiones que pasan por el gateway, antes de enrutarlas.
@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        // 1) Los endpoints internos son solo entre microservicios: nunca desde afuera
        if (path.contains("/interno")) {
            return error(exchange.getResponse(), HttpStatus.FORBIDDEN, "Acceso no permitido");
        }

        // 2) Rutas públicas: pasan sin token
        if (esPublica(method, path)) {
            return chain.filter(exchange);
        }

        // 3) Todo lo demás exige "Authorization: Bearer <token>" válido
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return error(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Token requerido");
        }
        try {
            jwtUtil.validar(header.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            return error(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }

        // El header Authorization se reenvía tal cual: cada ms vuelve a validar el token y lee el rol
        return chain.filter(exchange);
    }

    private boolean esPublica(HttpMethod method, String path) {
        if (HttpMethod.POST.equals(method)
                && (path.equals("/api/auth/login") || path.equals("/api/auth/register"))) {
            return true;
        }
        // Webpay redirige al navegador aquí (GET o POST), sin token
        if ((HttpMethod.GET.equals(method) || HttpMethod.POST.equals(method))
                && path.equals("/api/pagos/webpay/retorno")) {
            return true;
        }
        // Las fotos de las sedes se cargan directo en la app (Coil/Glide) sin token
        return HttpMethod.GET.equals(method) && matcher.match("/api/sedes/fotos/**", path);
    }

    // Mismo formato JSON de error que los microservicios
    private Mono<Void> error(ServerHttpResponse response, HttpStatus status, String mensaje) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"status\":%d,\"error\":\"%s\",\"mensaje\":\"%s\",\"timestamp\":\"%s\"}",
                status.value(), status.getReasonPhrase(), mensaje, LocalDateTime.now());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    // Se ejecuta antes que los filtros de enrutamiento
    @Override
    public int getOrder() {
        return -1;
    }
}
