package dev.withstudy.service;

import dev.withstudy.domain.CommunityComment;
import dev.withstudy.domain.CommunityPost;
import dev.withstudy.domain.enums.PostCategory;
import dev.withstudy.dto.CommentRequest;
import dev.withstudy.dto.CommunityPostRequest;
import dev.withstudy.repository.CommunityCommentRepository;
import dev.withstudy.repository.CommunityPostRepository;
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

    public Page<CommunityPost> findPosts(PostCategory category, int page) {
        PageRequest pageable = PageRequest.of(page, 15);
        if (category == null) {
            return postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return postRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
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
