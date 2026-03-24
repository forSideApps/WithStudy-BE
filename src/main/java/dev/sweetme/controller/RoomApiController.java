package dev.sweetme.controller;

import dev.sweetme.dto.ApplicationRequest;
import dev.sweetme.dto.RoomCreateRequest;
import dev.sweetme.dto.RoomUpdateRequest;
import dev.sweetme.dto.response.ApplicationDto;
import dev.sweetme.dto.response.ManageDto;
import dev.sweetme.dto.response.RoomDetailDto;
import dev.sweetme.dto.response.RoomSummaryDto;
import dev.sweetme.service.OciStorageService;
import dev.sweetme.service.RoomApplicationService;
import dev.sweetme.service.RoomService;
import dev.sweetme.util.SessionHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomApiController {

    private final RoomService roomService;
    private final RoomApplicationService roomApplicationService;
    private final OciStorageService ociStorageService;

    @Value("${app.oci.namespace}")
    private String namespace;

    @Value("${app.oci.bucket}")
    private String bucket;

    @Value("${app.oci.region}")
    private String region;

    private String logoBaseUrl() {
        return String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/SweetMe/",
                region, namespace, bucket
        );
    }

    @GetMapping
    public Page<RoomSummaryDto> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jobRole,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return roomService.findAll(status, jobRole, keyword, page)
                .map(r -> RoomSummaryDto.from(r, logoBaseUrl()));
    }

    @GetMapping("/recent")
    public List<RoomSummaryDto> getRecent(@RequestParam(defaultValue = "6") int limit) {
        return roomService.findRecent(limit).stream()
                .map(r -> RoomSummaryDto.from(r, logoBaseUrl()))
                .toList();
    }

    @GetMapping("/theme/{themeId}")
    public Page<RoomSummaryDto> getByTheme(
            @PathVariable Long themeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jobRole,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return roomService.findByTheme(themeId, status, jobRole, keyword, page)
                .map(r -> RoomSummaryDto.from(r, logoBaseUrl()));
    }

    @GetMapping("/{id}")
    public RoomDetailDto getRoom(@PathVariable Long id) {
        return RoomDetailDto.from(roomService.findById(id), logoBaseUrl(), false);
    }

    @PostMapping
    public Map<String, Long> createRoom(
            @RequestParam Long themeId,
            @RequestBody RoomCreateRequest request,
            HttpServletRequest httpRequest) {
        String memberUsername = SessionHelper.getUsername(httpRequest);
        if (memberUsername != null) {
            request.setCreatorNickname(memberUsername);
        }
        var room = roomService.create(themeId, request, memberUsername);
        return Map.of("id", room.getId());
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Void> apply(
            @PathVariable Long id,
            @RequestBody ApplicationRequest request,
            HttpServletRequest httpRequest) {
        String memberUsername = SessionHelper.getUsername(httpRequest);
        if (memberUsername != null) request.setApplicantName(memberUsername);
        roomApplicationService.apply(id, request, memberUsername);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/manage/verify")
    public ResponseEntity<Map<String, Boolean>> verifyPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        if (canManage(id, body.get("password"), httpRequest)) {
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
    }

    @GetMapping("/{id}/manage/applications")
    public ResponseEntity<ManageDto> getApplications(
            @PathVariable Long id,
            @RequestParam(required = false) String password,
            HttpServletRequest httpRequest) {
        if (!canManage(id, password, httpRequest)) return unauthorized();
        var room = roomService.findById(id);
        var applications = roomApplicationService.findByRoom(id);
        List<ApplicationDto> appDtos = applications.stream()
                .map(ApplicationDto::from)
                .toList();
        long pendingCount = appDtos.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long approvedCount = appDtos.stream().filter(a -> "APPROVED".equals(a.getStatus())).count();
        ManageDto dto = new ManageDto(
                RoomDetailDto.from(room, logoBaseUrl(), true),
                appDtos,
                pendingCount,
                approvedCount
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<Void> approveApplication(
            @PathVariable Long id,
            @RequestParam Long roomId,
            @RequestParam(required = false) String password,
            HttpServletRequest httpRequest) {
        if (!canManage(roomId, password, httpRequest)) return unauthorized();
        roomApplicationService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long roomId,
            @RequestParam(required = false) String password,
            HttpServletRequest httpRequest) {
        if (!canManage(roomId, password, httpRequest)) return unauthorized();
        roomApplicationService.reject(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateRoom(
            @PathVariable Long id,
            @RequestParam(required = false) String password,
            @RequestBody RoomUpdateRequest request,
            HttpServletRequest httpRequest) {
        if (!canManage(id, password, httpRequest)) return unauthorized();
        roomService.updateRoom(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeRoom(
            @PathVariable Long id,
            @RequestParam(required = false) String password,
            HttpServletRequest httpRequest) {
        if (!canManage(id, password, httpRequest)) return unauthorized();
        roomService.closeRoom(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<Void> reopenRoom(
            @PathVariable Long id,
            @RequestParam(required = false) String password,
            HttpServletRequest httpRequest) {
        if (!canManage(id, password, httpRequest)) return unauthorized();
        roomService.reopenRoom(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAll(HttpServletRequest request) {
        if (!SessionHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "어드민 권한이 필요합니다."));
        }
        roomService.deleteAll();
        return ResponseEntity.ok(Map.of("message", "전체 삭제 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id, HttpServletRequest request) {
        String username = SessionHelper.getUsername(request);
        if (!SessionHelper.isAdmin(request) && !roomService.isOwner(id, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        roomService.delete(id);
        return ResponseEntity.ok().build();
    }

    private boolean canManage(Long roomId, String password, HttpServletRequest request) {
        if (roomService.isOwner(roomId, SessionHelper.getUsername(request))) return true;
        return roomService.verifyPassword(roomId, password);
    }

    private <T> ResponseEntity<T> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
