package dev.withstudy.repository;

import dev.withstudy.domain.Company;
import dev.withstudy.domain.Room;
import dev.withstudy.domain.enums.JobRole;
import dev.withstudy.domain.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.company = :company " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:jobRole IS NULL OR r.jobRole = :jobRole) " +
           "AND (:keyword IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.createdAt DESC")
    Page<Room> findByThemeFiltered(
            @Param("company") Company company,
            @Param("status") RoomStatus status,
            @Param("jobRole") JobRole jobRole,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT r FROM Room r ORDER BY r.createdAt DESC")
    List<Room> findRecentRooms(Pageable pageable);

    @Query("SELECT r FROM Room r WHERE " +
           "(:status IS NULL OR r.status = :status) " +
           "AND (:jobRole IS NULL OR r.jobRole = :jobRole) " +
           "AND (:keyword IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.createdAt DESC")
    Page<Room> findAllFiltered(
            @Param("status") RoomStatus status,
            @Param("jobRole") JobRole jobRole,
            @Param("keyword") String keyword,
            Pageable pageable);
}
