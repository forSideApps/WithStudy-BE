package dev.sweetme.domain;

import dev.sweetme.domain.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 200)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemberRole role;

    @Column(name = "job_role", length = 30)
    private String jobRole;

    @Column(name = "career_level", length = 20)
    private String careerLevel;

    @Column(name = "algo_grade", length = 20)
    private String algoGrade;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void updateProfile(String jobRole, String careerLevel, String algoGrade) {
        this.jobRole = jobRole;
        this.careerLevel = careerLevel;
        this.algoGrade = algoGrade;
    }
}
