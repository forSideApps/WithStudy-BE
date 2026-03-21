package dev.withstudy.service;

import dev.withstudy.domain.VisitorLog;
import dev.withstudy.repository.VisitorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorLogRepository visitorLogRepository;

    @Transactional
    public void record(String ip) {
        LocalDate today = LocalDate.now();
        if (!visitorLogRepository.existsByIpAndVisitDate(ip, today)) {
            visitorLogRepository.save(new VisitorLog(ip, today));
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        long today = visitorLogRepository.countByVisitDate(LocalDate.now());
        long total = visitorLogRepository.count();
        return Map.of("today", today, "total", total);
    }
}
