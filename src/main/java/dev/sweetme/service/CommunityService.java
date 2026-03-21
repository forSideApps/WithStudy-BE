package dev.sweetme.service;

import dev.sweetme.domain.CommunityComment;
import dev.sweetme.domain.CommunityPost;
import dev.sweetme.domain.enums.PostCategory;
import dev.sweetme.dto.CommentRequest;
import dev.sweetme.dto.CommunityPostRequest;
import dev.sweetme.repository.CommunityCommentRepository;
import dev.sweetme.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;

    public Page<CommunityPost> findPosts(PostCategory category, String keyword, int page) {
        PageRequest pageable = PageRequest.of(page, 15);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (category == null && !hasKeyword) return postRepository.findAllByOrderByCreatedAtDesc(pageable);
        if (category == null) return postRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(keyword, pageable);
        if (!hasKeyword) return postRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        return postRepository.findByCategoryAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(category, keyword, pageable);
    }

    public CommunityPost findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public void incrementView(Long id) {
        findById(id).incrementViewCount();
    }

    public java.util.List<CommunityComment> findComments(Long postId) {
        return commentRepository.findByPostOrderByCreatedAtAsc(findById(postId));
    }

    @Transactional
    public CommunityPost createPost(CommunityPostRequest request) {
        CommunityPost post = CommunityPost.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .authorName(request.getAuthorName())
                .build();
        return postRepository.save(post);
    }

    @Transactional
    public CommunityComment addComment(Long postId, CommentRequest request) {
        CommunityComment comment = CommunityComment.builder()
                .post(findById(postId))
                .content(request.getContent())
                .authorName(request.getAuthorName())
                .build();
        return commentRepository.save(comment);
    }
}
