package dev.withstudy.dto.response;

import dev.withstudy.domain.ReviewComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewCommentDto {
    private final Long id;
    private final String authorName;
    private final String content;
    private final LocalDateTime createdAt;

    private ReviewCommentDto(ReviewComment c) {
        this.id = c.getId();
        this.authorName = c.getAuthorName();
        this.content = c.getContent();
        this.createdAt = c.getCreatedAt();
    }

    public static ReviewCommentDto from(ReviewComment c) {
        return new ReviewCommentDto(c);
    }
}
