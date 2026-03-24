package dev.sweetme.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.sweetme.domain.CommunityPost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailDto {

    private Long id;
    private String category;
    private String categoryDisplay;
    private String title;
    private String authorName;
    private String memberUsername;
    private Integer viewCount;
    private int commentCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String content;
    private List<CommentDto> comments;

    public static PostDetailDto from(CommunityPost post) {
        List<CommentDto> commentDtos = post.getComments().stream()
                .map(CommentDto::from)
                .collect(Collectors.toList());
        return new PostDetailDto(
                post.getId(),
                post.getCategory().name(),
                post.getCategory().getDisplayName(),
                post.getTitle(),
                post.getAuthorName(),
                post.getMemberUsername(),
                post.getViewCount(),
                commentDtos.size(),
                post.getCreatedAt(),
                post.getContent(),
                commentDtos
        );
    }
}
