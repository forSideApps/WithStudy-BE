package dev.withstudy.repository;

import dev.withstudy.domain.Room;
import dev.withstudy.domain.RoomApplication;
import dev.withstudy.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomApplicationRepository extends JpaRepository<RoomApplication, Long> {

    List<RoomApplication> findByRoomOrderByCreatedAtDesc(Room room);

    List<RoomApplication> findByRoomAndStatusOrderByCreatedAtDesc(Room room, ApplicationStatus status);

    long countByRoomAndStatus(Room room, ApplicationStatus status);
}
