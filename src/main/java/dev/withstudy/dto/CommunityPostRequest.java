package dev.withstudy.dto;

import dev.withstudy.domain.enums.PostCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostRequest {

    @NotNull(message = "카테고리를 선택해주세요.")
    private PostCategory category;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 5000, message = "내용은 5000자 이내로 입력해주세요.")
    private String content;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 50자 이내로 입력해주세요.")
    private String authorName;
}
