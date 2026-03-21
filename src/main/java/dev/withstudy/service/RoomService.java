package dev.withstudy.service;

import dev.withstudy.domain.Company;
import dev.withstudy.domain.Room;
import dev.withstudy.domain.enums.JobRole;
import dev.withstudy.domain.enums.RoomStatus;
import dev.withstudy.dto.RoomCreateRequest;
import dev.withstudy.dto.RoomUpdateRequest;
import dev.withstudy.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final CompanyService companyService;
    private final BCryptPasswordEncoder passwordEncoder;

    public Page<Room> findByTheme(Long companyId, String statusFilter, String jobRoleFilter, String keyword, int page) {
        Company company = companyService.findById(companyId);
        return roomRepository.findByThemeFiltered(
                company, parseStatus(statusFilter), parseJobRole(jobRoleFilter), parseKeyword(keyword),
                PageRequest.of(page, 10));
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다."));
    }

    public List<Room> findRecent(int limit) {
        return roomRepository.findRecentRooms(PageRequest.of(0, limit));
    }

    public Page<Room> findAll(String statusFilter, String jobRoleFilter, String keyword, int page) {
        return roomRepository.findAllFiltered(
                parseStatus(statusFilter), parseJobRole(jobRoleFilter), parseKeyword(keyword),
                PageRequest.of(page, 10));
    }

    @Transactional
    public Room create(Long companyId, RoomCreateRequest request) {
        Room room = Room.builder()
                .company(companyService.findById(companyId))
                .title(request.getTitle())
                .description(request.getDescription())
                .maxMembers(request.getMaxMembers())
                .creatorNickname(request.getCreatorNickname())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .kakaoLink(request.getKakaoLink())
                .requirements(request.getRequirements())
                .jobRole(request.getJobRole())
                .build();
        return roomRepository.save(room);
    }

    public boolean verifyPassword(Long roomId, String rawPassword) {
        return passwordEncoder.matches(rawPassword, findById(roomId).getPasswordHash());
    }

    @Transactional
    public void closeRoom(Long roomId) {
        findById(roomId).close();
    }

    @Transactional
    public Room updateRoom(Long roomId, RoomUpdateRequest request) {
        Room room = findById(roomId);
        room.update(request.getTitle(), request.getDescription(), request.getMaxMembers(),
                request.getKakaoLink(), request.getRequirements(), request.getJobRole());
        return room;
    }

    private RoomStatus parseStatus(String value) {
        return (value != null && !value.isBlank()) ? RoomStatus.valueOf(value) : null;
    }

    private JobRole parseJobRole(String value) {
        return (value != null && !value.isBlank()) ? JobRole.valueOf(value) : null;
    }

    private String parseKeyword(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }
}
