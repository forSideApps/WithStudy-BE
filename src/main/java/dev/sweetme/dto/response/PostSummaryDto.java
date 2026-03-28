package dev.sweetme.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.sweetme.domain.CommunityPost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostSummaryDto {

    private Long id;
    private String category;
    private String categoryDisplay;
    private String title;
    private String authorName;
    private Integer viewCount;
    private int commentCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static PostSummaryDto from(CommunityPost post) {
        return new PostSummaryDto(
                post.getId(),
                post.getCategory().name(),
                post.getCategory().getDisplayName(),
                post.getTitle(),
                post.getAuthorName(),
                post.getViewCount(),
                post.getCommentCount(),
                post.getCreatedAt()
        );
    }
}
