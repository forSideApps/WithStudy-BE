package dev.withstudy.repository;

import dev.withstudy.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findBySlug(String slug);

    @Query("SELECT c FROM Company c ORDER BY c.displayOrder ASC")
    List<Company> findAllOrderByDisplayOrder();

    boolean existsBySlug(String slug);
}
