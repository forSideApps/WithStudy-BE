package dev.sweetme.repository;

import dev.sweetme.domain.Company;
import dev.sweetme.domain.Room;
import dev.sweetme.domain.enums.JobRole;
import dev.sweetme.domain.enums.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") Long id);

    @Query(value = "SELECT r FROM Room r JOIN FETCH r.company WHERE r.company = :company " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:jobRole IS NULL OR r.jobRole = :jobRole) " +
           "AND (:keyword = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Room r WHERE r.company = :company " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:jobRole IS NULL OR r.jobRole = :jobRole) " +
           "AND (:keyword = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Room> findByThemeFiltered(
            @Param("company") Company company,
            @Param("status") RoomStatus status,
            @Param("jobRole") JobRole jobRole,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT r FROM Room r JOIN FETCH r.company ORDER BY r.createdAt DESC")
    List<Room> findRecentRooms(Pageable pageable);

    @Query("SELECT r FROM Room r JOIN FETCH r.company WHERE r.memberUsername = :username ORDER BY r.createdAt DESC")
    List<Room> findByMemberUsernameOrderByCreatedAtDesc(@Param("username") String username);

    @Query(value = "SELECT r FROM Room r JOIN FETCH r.company WHERE " +
           "(:status IS NULL OR r.status = :status) " +
           "AND (:jobRole IS NULL OR r.jobRole = :jobRole) " +
           "AND (:keyword = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Room r WHERE " +
           "(:status IS NULL OR r.status = :status) " +
           "AND (:jobRole IS NULL OR r.jobRole = :jobRole) " +
           "AND (:keyword = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Room> findAllFiltered(
            @Param("status") RoomStatus status,
            @Param("jobRole") JobRole jobRole,
            @Param("keyword") String keyword,
            Pageable pageable);
}
