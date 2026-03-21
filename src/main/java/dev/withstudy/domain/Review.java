package dev.withstudy.domain;

import dev.withstudy.domain.enums.CareerLevel;
import dev.withstudy.domain.enums.ReviewJobCategory;
import dev.withstudy.domain.enums.ReviewStatus;
import dev.withstudy.domain.enums.ReviewType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_review")
    @SequenceGenerator(name = "seq_review", sequenceName = "SEQ_REVIEW", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR2(20) DEFAULT 'PENDING'")
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_category", nullable = false, columnDefinition = "VARCHAR2(20) DEFAULT 'BACKEND'")
    private ReviewJobCategory jobCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", nullable = false, columnDefinition = "VARCHAR2(20) DEFAULT 'JUNIOR'")
    private CareerLevel careerLevel;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "author_name", nullable = false, length = 50)
    private String authorName;

    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ReviewComment> comments = new ArrayList<>();

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(ReviewType type, ReviewJobCategory jobCategory, CareerLevel careerLevel,
                       String title, String content, String contactInfo) {
        this.type = type;
        this.jobCategory = jobCategory;
        this.careerLevel = careerLevel;
        this.title = title;
        this.content = content;
        this.contactInfo = contactInfo;
    }

    public void markDone() {
        this.status = ReviewStatus.DONE;
    }

    public void markPending() {
        this.status = ReviewStatus.PENDING;
    }
}
