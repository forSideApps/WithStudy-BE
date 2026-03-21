package dev.withstudy.controller;

import dev.withstudy.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @PostMapping("/api/visitors/ping")
    public ResponseEntity<Void> ping(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        visitorService.record(ip);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/visitors")
    public Map<String, Long> getStats() {
        return visitorService.getStats();
    }
}
