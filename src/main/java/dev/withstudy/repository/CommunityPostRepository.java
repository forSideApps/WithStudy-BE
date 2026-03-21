package dev.withstudy.repository;

import dev.withstudy.domain.CommunityPost;
import dev.withstudy.domain.enums.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findByCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable);

    Page<CommunityPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
