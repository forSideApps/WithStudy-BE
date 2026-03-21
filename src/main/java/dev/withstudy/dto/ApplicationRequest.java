package dev.withstudy.dto;

import dev.withstudy.domain.enums.AlgoGrade;
import dev.withstudy.domain.enums.JobRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 50자 이내로 입력해주세요.")
    private String applicantName;

    @NotNull(message = "직군을 선택해주세요.")
    private JobRole jobRole;

    private AlgoGrade algoGrade;

    @Min(value = 0, message = "0 이상의 숫자를 입력해주세요.")
    @Max(value = 99, message = "99 이하의 숫자를 입력해주세요.")
    private Integer interviewCount;

    @Size(max = 1000, message = "자기소개는 1000자 이내로 입력해주세요.")
    private String introduction;

    @Size(max = 200, message = "연락처는 200자 이내로 입력해주세요.")
    private String contactInfo;
}
