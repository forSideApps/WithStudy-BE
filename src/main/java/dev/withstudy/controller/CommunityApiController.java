package dev.withstudy.controller;

import dev.withstudy.domain.enums.PostCategory;
import dev.withstudy.dto.CommentRequest;
import dev.withstudy.dto.CommunityPostRequest;
import dev.withstudy.dto.response.PostDetailDto;
import dev.withstudy.dto.response.PostSummaryDto;
import dev.withstudy.service.CommunityService;
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
            @RequestParam(defaultValue = "0") int page) {
        PostCategory postCategory = null;
        if (category != null && !category.isBlank()) {
            postCategory = PostCategory.valueOf(category);
        }
        return communityService.findPosts(postCategory, page)
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
    public Map<String, Long> createPost(@RequestBody CommunityPostRequest request) {
        var post = communityService.createPost(request);
        return Map.of("id", post.getId());
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request) {
        communityService.addComment(id, request);
        return ResponseEntity.ok().build();
    }
}
