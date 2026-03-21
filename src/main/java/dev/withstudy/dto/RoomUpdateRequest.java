package dev.withstudy.dto;

import dev.withstudy.domain.enums.JobRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomUpdateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "최대 인원을 입력해주세요.")
    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 20, message = "최대 20명까지 설정 가능합니다.")
    private Integer maxMembers;

    @Size(max = 500)
    private String kakaoLink;

    @Size(max = 500)
    private String requirements;

    private JobRole jobRole;
}
