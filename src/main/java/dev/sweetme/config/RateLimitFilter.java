package dev.sweetme.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 단순 IP 기반 Rate Limiter.
 * 1분 슬라이딩 윈도우 내 동일 IP의 요청을 MAX_REQUESTS 이하로 제한.
 * Redis가 없는 단일 인스턴스 환경용. 멀티 인스턴스 환경이면 Redis + Bucket4j 사용 권장.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 100;       // 1분당 최대 요청 수
    private static final long WINDOW_MS   = 60_000L;   // 윈도우 크기: 1분

    private record RequestCount(AtomicInteger count, long windowStart) {}

    private final Map<String, RequestCount> ipCountMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        long now  = System.currentTimeMillis();

        RequestCount rc = ipCountMap.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart() > WINDOW_MS) {
                return new RequestCount(new AtomicInteger(1), now);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (rc.count().get() > MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Too Many Requests\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
