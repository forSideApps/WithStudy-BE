package dev.withstudy.repository;

import dev.withstudy.domain.Review;
import dev.withstudy.domain.enums.CareerLevel;
import dev.withstudy.domain.enums.ReviewJobCategory;
import dev.withstudy.domain.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE " +
           "(:type IS NULL OR r.type = :type) " +
           "AND (:jobCategory IS NULL OR r.jobCategory = :jobCategory) " +
           "AND (:careerLevel IS NULL OR r.careerLevel = :careerLevel) " +
           "AND (:keyword IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(r.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.createdAt DESC")
    Page<Review> search(
            @Param("type") ReviewType type,
            @Param("jobCategory") ReviewJobCategory jobCategory,
            @Param("careerLevel") CareerLevel careerLevel,
            @Param("keyword") String keyword,
            Pageable pageable);
}
