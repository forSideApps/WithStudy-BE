package dev.withstudy.dto.response;

import dev.withstudy.domain.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewSummaryDto {
    private final Long id;
    private final String type;
    private final String typeDisplayName;
    private final String status;
    private final String statusDisplayName;
    private final String jobCategory;
    private final String jobCategoryDisplayName;
    private final String careerLevel;
    private final String careerLevelDisplayName;
    private final String title;
    private final String authorName;
    private final Integer viewCount;
    private final int commentCount;
    private final LocalDateTime createdAt;

    private ReviewSummaryDto(Review r) {
        this.id = r.getId();
        this.type = r.getType().name();
        this.typeDisplayName = r.getType().getDisplayName();
        this.status = r.getStatus().name();
        this.statusDisplayName = r.getStatus().getDisplayName();
        this.jobCategory = r.getJobCategory().name();
        this.jobCategoryDisplayName = r.getJobCategory().getDisplayName();
        this.careerLevel = r.getCareerLevel().name();
        this.careerLevelDisplayName = r.getCareerLevel().getDisplayName();
        this.title = r.getTitle();
        this.authorName = r.getAuthorName();
        this.viewCount = r.getViewCount();
        this.commentCount = r.getComments().size();
        this.createdAt = r.getCreatedAt();
    }

    public static ReviewSummaryDto from(Review r) {
        return new ReviewSummaryDto(r);
    }
}
