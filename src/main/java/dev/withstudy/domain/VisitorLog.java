package dev.withstudy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "visitor_log",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ip", "visit_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String ip;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    public VisitorLog(String ip, LocalDate visitDate) {
        this.ip = ip;
        this.visitDate = visitDate;
    }
}
