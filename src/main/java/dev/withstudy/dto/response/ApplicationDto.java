package dev.withstudy.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.withstudy.domain.RoomApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDto {

    private Long id;
    private String applicantName;
    private String jobRole;
    private String jobRoleDisplay;
    private String algoGrade;
    private String algoGradeDisplay;
    private Integer interviewCount;
    private String introduction;
    private String contactInfo;
    private String status;
    private String statusDisplay;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static ApplicationDto from(RoomApplication app) {
        return new ApplicationDto(
                app.getId(),
                app.getApplicantName(),
                app.getJobRole() != null ? app.getJobRole().name() : null,
                app.getJobRole() != null ? app.getJobRole().getDisplayName() : null,
                app.getAlgoGrade() != null ? app.getAlgoGrade().name() : null,
                app.getAlgoGrade() != null ? app.getAlgoGrade().getDisplayName() : null,
                app.getInterviewCount(),
                app.getIntroduction(),
                app.getContactInfo(),
                app.getStatus().name(),
                app.getStatus().getDisplayName(),
                app.getCreatedAt()
        );
    }
}
