package dev.withstudy.repository;

import dev.withstudy.domain.CommunityComment;
import dev.withstudy.domain.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findByPostOrderByCreatedAtAsc(CommunityPost post);
}
