package dev.sweetme.service;

import dev.sweetme.domain.Room;
import dev.sweetme.domain.RoomApplication;
import dev.sweetme.domain.enums.ApplicationStatus;
import dev.sweetme.dto.ApplicationRequest;
import dev.sweetme.repository.RoomApplicationRepository;
import dev.sweetme.repository.RoomRepository;
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
    private final RoomRepository roomRepository;

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
        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다."));
        if (room.getStatus() != dev.sweetme.domain.enums.RoomStatus.OPEN) {
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
