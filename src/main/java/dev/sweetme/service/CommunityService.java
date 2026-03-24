package dev.sweetme.service;

import dev.sweetme.domain.CommunityComment;
import dev.sweetme.domain.CommunityPost;
import dev.sweetme.domain.enums.PostCategory;
import dev.sweetme.dto.CommentRequest;
import dev.sweetme.dto.CommunityPostRequest;
import dev.sweetme.repository.CommunityCommentRepository;
import dev.sweetme.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.page.community-size:15}")
    private int pageSize;

    public Page<CommunityPost> findPosts(PostCategory category, String keyword, int page) {
        PageRequest pageable = PageRequest.of(page, pageSize);
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

    @Transactional
    public CommunityPost createPost(CommunityPostRequest request, String memberUsername) {
        CommunityPost post = CommunityPost.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .authorName(request.getAuthorName())
                .memberUsername(memberUsername)
                .build();
        return postRepository.save(post);
    }

    public boolean isOwner(Long postId, String username) {
        if (username == null) return false;
        return username.equals(findById(postId).getMemberUsername());
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.delete(findById(id));
    }

    @Transactional
    public CommunityComment addComment(Long postId, CommentRequest request, String memberUsername) {
        CommunityComment comment = CommunityComment.builder()
                .post(findById(postId))
                .content(request.getContent())
                .authorName(request.getAuthorName())
                .memberUsername(memberUsername)
                .build();
        return commentRepository.save(comment);
    }

    @Transactional
    public void updateComment(Long commentId, String content, String memberUsername, boolean isAdmin) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!isAdmin && (memberUsername == null || !memberUsername.equals(comment.getMemberUsername()))) {
            throw new SecurityException("수정 권한이 없습니다.");
        }
        comment.updateContent(content);
    }

    @Transactional
    public void deleteComment(Long commentId, String memberUsername, boolean isAdmin) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!isAdmin && (memberUsername == null || !memberUsername.equals(comment.getMemberUsername()))) {
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
}
