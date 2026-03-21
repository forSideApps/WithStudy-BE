package dev.withstudy.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.withstudy.domain.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomDetailDto {

    private Long id;
    private String title;
    private String status;
    private String statusDisplay;
    private Integer maxMembers;
    private String creatorNickname;
    private long pendingCount;
    private long approvedCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String themeName;
    private Long themeId;
    private String themeSlug;
    private String themeLogoUrl;
    private String description;
    private String requirements;
    private String kakaoLink;
    private String jobRole;
    private String jobRoleDisplay;

    public static RoomDetailDto from(Room room, String logoBaseUrl) {
        return from(room, logoBaseUrl, false);
    }

    public static RoomDetailDto from(Room room, String logoBaseUrl, boolean includeKakaoLink) {
        return new RoomDetailDto(
                room.getId(),
                room.getTitle(),
                room.getStatus().name(),
                room.getStatus().getDisplayName(),
                room.getMaxMembers(),
                room.getCreatorNickname(),
                room.getPendingCount(),
                room.getApprovedCount(),
                room.getCreatedAt(),
                room.getCompany().getName(),
                room.getCompany().getId(),
                room.getCompany().getSlug(),
                logoBaseUrl + room.getCompany().getSlug() + ".png",
                room.getDescription(),
                room.getRequirements(),
                includeKakaoLink ? room.getKakaoLink() : null,
                room.getJobRole() != null ? room.getJobRole().name() : null,
                room.getJobRole() != null ? room.getJobRole().getDisplayName() : null
        );
    }
}
