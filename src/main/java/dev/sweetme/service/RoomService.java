package dev.sweetme.service;

import dev.sweetme.domain.Company;
import dev.sweetme.domain.Room;
import dev.sweetme.domain.enums.JobRole;
import dev.sweetme.domain.enums.RoomStatus;
import dev.sweetme.dto.RoomCreateRequest;
import dev.sweetme.dto.RoomUpdateRequest;
import dev.sweetme.repository.RoomApplicationRepository;
import dev.sweetme.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final RoomApplicationRepository roomApplicationRepository;
    private final CompanyService companyService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.page.room-size:10}")
    private int pageSize;

    public Page<Room> findByTheme(Long companyId, String statusFilter, String jobRoleFilter, String keyword, int page) {
        Company company = companyService.findById(companyId);
        return roomRepository.findByThemeFiltered(
                company, parseStatus(statusFilter), parseJobRole(jobRoleFilter), parseKeyword(keyword),
                PageRequest.of(page, pageSize));
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
                PageRequest.of(page, pageSize));
    }

    @Transactional
    public Room create(Long companyId, RoomCreateRequest request, String memberUsername) {
        String hash = (request.getPassword() != null && !request.getPassword().isBlank())
                ? passwordEncoder.encode(request.getPassword()) : null;
        Room room = Room.builder()
                .company(companyService.findById(companyId))
                .title(request.getTitle())
                .description(request.getDescription())
                .maxMembers(request.getMaxMembers())
                .creatorNickname(request.getCreatorNickname())
                .passwordHash(hash)
                .kakaoLink(request.getKakaoLink())
                .requirements(request.getRequirements())
                .jobRole(request.getJobRole())
                .memberUsername(memberUsername)
                .build();
        return roomRepository.save(room);
    }

    public boolean verifyPassword(Long roomId, String rawPassword) {
        String hash = findById(roomId).getPasswordHash();
        return hash != null && passwordEncoder.matches(rawPassword, hash);
    }

    public boolean isOwner(Long roomId, String username) {
        if (username == null) return false;
        String ownerUsername = findById(roomId).getMemberUsername();
        return username.equals(ownerUsername);
    }

    @Transactional
    public void closeRoom(Long roomId) {
        findById(roomId).close();
    }

    @Transactional
    public void reopenRoom(Long roomId) {
        findById(roomId).updateStatus(RoomStatus.OPEN);
    }

    @Transactional
    public Room updateRoom(Long roomId, RoomUpdateRequest request) {
        Room room = findById(roomId);
        room.update(request.getTitle(), request.getDescription(), request.getMaxMembers(),
                request.getKakaoLink(), request.getRequirements(), request.getJobRole());
        return room;
    }

    @Transactional
    public void delete(Long id) {
        roomRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        roomApplicationRepository.deleteAllInBatch();
        roomRepository.deleteAllInBatch();
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
