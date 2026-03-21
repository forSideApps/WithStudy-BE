package dev.withstudy.dto;

import dev.withstudy.domain.enums.JobRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateRequest {

    @NotBlank(message = "방 제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    private String title;

    @Size(max = 2000, message = "설명은 2000자 이내로 입력해주세요.")
    private String description;

    @NotNull(message = "최대 인원을 선택해주세요.")
    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 10, message = "최대 10명까지 가능합니다.")
    private Integer maxMembers;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 50자 이내로 입력해주세요.")
    private String creatorNickname;

    @NotBlank(message = "방 관리 비밀번호를 설정해주세요.")
    @Size(min = 4, max = 20, message = "비밀번호는 4~20자로 입력해주세요.")
    private String password;

    @NotBlank(message = "카카오 오픈채팅 링크를 입력해주세요.")
    @Size(max = 500, message = "카카오 오픈채팅 링크는 500자 이내로 입력해주세요.")
    private String kakaoLink;

    @Size(max = 500, message = "요구사항은 500자 이내로 입력해주세요.")
    private String requirements;

    @NotNull(message = "직군을 선택해주세요.")
    private JobRole jobRole;
}
