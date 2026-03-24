package dev.sweetme.controller;

import dev.sweetme.domain.enums.PostCategory;
import dev.sweetme.dto.CommentRequest;
import dev.sweetme.dto.CommunityPostRequest;
import dev.sweetme.dto.response.PostDetailDto;
import dev.sweetme.dto.response.PostSummaryDto;
import dev.sweetme.service.CommunityService;
import dev.sweetme.util.SessionHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityApiController {

    private final CommunityService communityService;

    @GetMapping
    public Page<PostSummaryDto> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        PostCategory postCategory = null;
        if (category != null && !category.isBlank()) {
            postCategory = PostCategory.valueOf(category);
        }
        return communityService.findPosts(postCategory, keyword, page)
                .map(PostSummaryDto::from);
    }

    @GetMapping("/{id}")
    public PostDetailDto getPost(@PathVariable Long id) {
        return PostDetailDto.from(communityService.findById(id));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Long id) {
        communityService.incrementView(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestBody CommunityPostRequest request,
            HttpServletRequest httpRequest) {
        if (PostCategory.NOTICE == request.getCategory()) {
            if (!isAdmin(httpRequest)) {
                return ResponseEntity.status(403).body(Map.of("message", "공지사항은 어드민만 작성할 수 있습니다."));
            }
        }
        String memberUsername = getSessionUsername(httpRequest);
        if (memberUsername != null) request.setAuthorName(memberUsername);
        var post = communityService.createPost(request, memberUsername);
        return ResponseEntity.ok(Map.of("id", post.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, HttpServletRequest httpRequest) {
        String username = getSessionUsername(httpRequest);
        if (!isAdmin(httpRequest) && !communityService.isOwner(id, username)) {
            return ResponseEntity.status(403).build();
        }
        communityService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {
        String memberUsername = getSessionUsername(httpRequest);
        if (memberUsername != null) {
            request.setAuthorName(memberUsername);
        }
        communityService.addComment(id, request, memberUsername);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        communityService.updateComment(commentId, body.get("content"), getSessionUsername(httpRequest), isAdmin(httpRequest));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        String memberUsername = getSessionUsername(httpRequest);
        communityService.deleteComment(commentId, memberUsername, isAdmin(httpRequest));
        return ResponseEntity.ok().build();
    }

    private String getSessionUsername(HttpServletRequest request) {
        return SessionHelper.getUsername(request);
    }

    private boolean isAdmin(HttpServletRequest request) {
        return SessionHelper.isAdmin(request);
    }
}
