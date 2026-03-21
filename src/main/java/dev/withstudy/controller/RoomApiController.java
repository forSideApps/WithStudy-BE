package dev.withstudy.controller;

import dev.withstudy.dto.ApplicationRequest;
import dev.withstudy.dto.RoomCreateRequest;
import dev.withstudy.dto.RoomUpdateRequest;
import dev.withstudy.dto.response.ApplicationDto;
import dev.withstudy.dto.response.ManageDto;
import dev.withstudy.dto.response.RoomDetailDto;
import dev.withstudy.dto.response.RoomSummaryDto;
import dev.withstudy.service.OciStorageService;
import dev.withstudy.service.RoomApplicationService;
import dev.withstudy.service.RoomService;
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
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/WithStudy/",
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
            @RequestBody RoomCreateRequest request) {
        var room = roomService.create(themeId, request);
        return Map.of("id", room.getId());
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Void> apply(
            @PathVariable Long id,
            @RequestBody ApplicationRequest request) {
        roomApplicationService.apply(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/manage/verify")
    public ResponseEntity<Map<String, Boolean>> verifyPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        boolean valid = roomService.verifyPassword(id, body.get("password"));
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{id}/manage/applications")
    public ResponseEntity<ManageDto> getApplications(
            @PathVariable Long id,
            @RequestParam String password) {
        if (!roomService.verifyPassword(id, password)) return unauthorized();
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
            @RequestParam String password) {
        if (!roomService.verifyPassword(roomId, password)) return unauthorized();
        roomApplicationService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable Long id,
            @RequestParam Long roomId,
            @RequestParam String password) {
        if (!roomService.verifyPassword(roomId, password)) return unauthorized();
        roomApplicationService.reject(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateRoom(
            @PathVariable Long id,
            @RequestParam String password,
            @RequestBody RoomUpdateRequest request) {
        if (!roomService.verifyPassword(id, password)) return unauthorized();
        roomService.updateRoom(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeRoom(
            @PathVariable Long id,
            @RequestParam String password) {
        if (!roomService.verifyPassword(id, password)) return unauthorized();
        roomService.closeRoom(id);
        return ResponseEntity.ok().build();
    }

    private <T> ResponseEntity<T> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
