package dev.withstudy.dto.response;

import dev.withstudy.domain.Review;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ReviewDetailDto {
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
    private final String content;
    private final String authorName;
    private final String contactInfo;
    private final Integer viewCount;
    private final LocalDateTime createdAt;
    private final List<ReviewCommentDto> comments;

    private ReviewDetailDto(Review r) {
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
        this.content = r.getContent();
        this.authorName = r.getAuthorName();
        this.contactInfo = r.getContactInfo();
        this.viewCount = r.getViewCount();
        this.createdAt = r.getCreatedAt();
        this.comments = r.getComments().stream()
                .map(ReviewCommentDto::from)
                .collect(Collectors.toList());
    }

    public static ReviewDetailDto from(Review r) {
        return new ReviewDetailDto(r);
    }
}
