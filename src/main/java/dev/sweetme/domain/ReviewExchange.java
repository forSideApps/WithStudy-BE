package dev.sweetme.domain;

import dev.sweetme.domain.enums.ExchangeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_exchange",
       uniqueConstraints = @UniqueConstraint(columnNames = {"requester_review_id", "target_review_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_review_exchange")
    @SequenceGenerator(name = "seq_review_exchange", sequenceName = "SEQ_REVIEW_EXCHANGE", allocationSize = 1)
    private Long id;

    @Column(name = "requester_review_id", nullable = false)
    private Long requesterReviewId;

    @Column(name = "target_review_id", nullable = false)
    private Long targetReviewId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_status", nullable = false, columnDefinition = "VARCHAR2(20) DEFAULT 'ACCEPTED'")
    @Builder.Default
    private ExchangeStatus status = ExchangeStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void accept() {
        this.status = ExchangeStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ExchangeStatus.REJECTED;
    }
}
