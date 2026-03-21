package dev.sweetme.repository;

import dev.sweetme.domain.CommunityPost;
import dev.sweetme.domain.enums.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable);

    Page<CommunityPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<CommunityPost> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    Page<CommunityPost> findByCategoryAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(PostCategory category, String keyword, Pageable pageable);
}
