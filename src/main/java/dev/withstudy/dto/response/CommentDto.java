package dev.withstudy.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.withstudy.domain.CommunityComment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;
    private String authorName;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static CommentDto from(CommunityComment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getAuthorName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
