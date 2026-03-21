package dev.withstudy.service;

import dev.withstudy.domain.Room;
import dev.withstudy.domain.RoomApplication;
import dev.withstudy.domain.enums.ApplicationStatus;
import dev.withstudy.dto.ApplicationRequest;
import dev.withstudy.repository.RoomApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomApplicationService {

    private final RoomApplicationRepository applicationRepository;
    private final RoomService roomService;

    public List<RoomApplication> findByRoom(Long roomId) {
        Room room = roomService.findById(roomId);
        return applicationRepository.findByRoomOrderByCreatedAtDesc(room);
    }

    public List<RoomApplication> findByRoomAndStatus(Long roomId, ApplicationStatus status) {
        Room room = roomService.findById(roomId);
        return applicationRepository.findByRoomAndStatusOrderByCreatedAtDesc(room, status);
    }

    @Transactional
    public RoomApplication apply(Long roomId, ApplicationRequest request) {
        Room room = roomService.findById(roomId);
        if (room.getStatus() != dev.withstudy.domain.enums.RoomStatus.OPEN) {
            throw new IllegalStateException("모집이 마감된 방입니다.");
        }
        RoomApplication application = RoomApplication.builder()
                .room(room)
                .applicantName(request.getApplicantName())
                .jobRole(request.getJobRole())
                .algoGrade(request.getAlgoGrade())
                .interviewCount(request.getInterviewCount())
                .introduction(request.getIntroduction())
                .contactInfo(request.getContactInfo())
                .build();
        return applicationRepository.save(application);
    }

    @Transactional
    public void approve(Long applicationId) {
        RoomApplication application = findById(applicationId);
        application.approve();
    }

    @Transactional
    public void reject(Long applicationId) {
        RoomApplication application = findById(applicationId);
        application.reject();
    }

    private RoomApplication findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다."));
    }
}
