package dev.withstudy.domain;

import dev.withstudy.domain.enums.AlgoGrade;
import dev.withstudy.domain.enums.ApplicationStatus;
import dev.withstudy.domain.enums.JobRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_application")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_room_application")
    @SequenceGenerator(name = "seq_room_application", sequenceName = "SEQ_ROOM_APPLICATION", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "algo_grade")
    private AlgoGrade algoGrade;

    @Column(name = "interview_count")
    private Integer interviewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_role")
    private JobRole jobRole;

    @Column(length = 1000)
    private String introduction;

    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }
}
