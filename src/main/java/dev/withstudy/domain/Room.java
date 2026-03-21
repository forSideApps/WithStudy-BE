package dev.withstudy.domain;

import dev.withstudy.domain.enums.ApplicationStatus;
import dev.withstudy.domain.enums.JobRole;
import dev.withstudy.domain.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_room")
    @SequenceGenerator(name = "seq_room", sequenceName = "SEQ_ROOM", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "current_members")
    @Builder.Default
    private Integer currentMembers = 0;

    @Column(name = "kakao_link", length = 500)
    private String kakaoLink;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "creator_nickname", nullable = false, length = 50)
    private String creatorNickname;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomStatus status = RoomStatus.OPEN;

    @Column(name = "requirements", length = 500)
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_role", length = 20)
    private JobRole jobRole;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RoomApplication> applications = new ArrayList<>();

    public void close() {
        this.status = RoomStatus.CLOSED;
    }

    public void update(String title, String description, Integer maxMembers,
                       String kakaoLink, String requirements, JobRole jobRole) {
        this.title = title;
        this.description = description;
        this.maxMembers = maxMembers;
        this.kakaoLink = kakaoLink;
        this.requirements = requirements;
        this.jobRole = jobRole;
    }

    public void updateStatus(RoomStatus status) {
        this.status = status;
    }

    public long getPendingCount() {
        return applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING)
                .count();
    }

    public long getApprovedCount() {
        return applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPROVED)
                .count();
    }
}
