package dev.sweetme.domain;

import dev.sweetme.domain.enums.ApplicationStatus;
import dev.sweetme.domain.enums.JobRole;
import dev.sweetme.domain.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
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

    @Column(name = "member_username", length = 50)
    private String memberUsername;

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

    @Formula("(SELECT COUNT(*) FROM room_application a WHERE a.room_id = id AND a.status = 'PENDING')")
    private long pendingCount;

    @Formula("(SELECT COUNT(*) FROM room_application a WHERE a.room_id = id AND a.status = 'APPROVED')")
    private long approvedCount;

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
        return pendingCount;
    }

    public long getApprovedCount() {
        return approvedCount;
    }
}
