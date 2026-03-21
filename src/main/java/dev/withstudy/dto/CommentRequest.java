package dev.withstudy.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 50자 이내로 입력해주세요.")
    private String authorName;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(min = 2, max = 1000, message = "댓글은 2~1000자로 입력해주세요.")
    private String content;
}
