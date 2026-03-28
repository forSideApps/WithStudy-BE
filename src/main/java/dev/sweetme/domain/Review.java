package dev.sweetme.domain;

import dev.sweetme.domain.enums.CareerLevel;
import dev.sweetme.domain.enums.ReviewJobCategory;
import dev.sweetme.domain.enums.ReviewStatus;
import dev.sweetme.domain.enums.ReviewType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_category", nullable = false)
    private ReviewJobCategory jobCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", nullable = false)
    private CareerLevel careerLevel;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    @Column(name = "portfolio_link", length = 500)
    private String portfolioLink;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "member_username", length = 50)
    private String memberUsername;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Formula("(SELECT COUNT(*) FROM review_comment c WHERE c.review_id = id)")
    private int commentCount;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    @BatchSize(size = 15)
    @Builder.Default
    private List<ReviewComment> comments = new ArrayList<>();

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(ReviewType type, ReviewJobCategory jobCategory, CareerLevel careerLevel,
                       String title, String content, String contactInfo, String portfolioLink) {
        this.type = type;
        this.jobCategory = jobCategory;
        this.careerLevel = careerLevel;
        this.title = title;
        this.content = content;
        this.contactInfo = contactInfo;
        this.portfolioLink = portfolioLink;
    }

    public void markDone() {
        this.status = ReviewStatus.DONE;
    }

    public void markPending() {
        this.status = ReviewStatus.PENDING;
    }
}
