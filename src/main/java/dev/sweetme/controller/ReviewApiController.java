package dev.sweetme.controller;

import dev.sweetme.dto.CommentRequest;
import dev.sweetme.dto.CommentUpdateRequest;
import dev.sweetme.dto.ReviewRequest;
import dev.sweetme.dto.ReviewUpdateRequest;
import dev.sweetme.dto.response.ReviewDetailDto;
import dev.sweetme.dto.response.ReviewSummaryDto;
import dev.sweetme.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping
    public Page<ReviewSummaryDto> getReviews(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String jobCategory,
            @RequestParam(required = false) String careerLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return reviewService.findReviews(type, status, jobCategory, careerLevel, keyword, page)
                .map(ReviewSummaryDto::from);
    }

    @GetMapping("/{id}")
    public ReviewDetailDto getReview(@PathVariable Long id) {
        return ReviewDetailDto.from(reviewService.findById(id));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Long id) {
        reviewService.incrementView(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public Map<String, Long> createReview(@RequestBody ReviewRequest request) {
        var review = reviewService.create(request);
        return Map.of("id", review.getId());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<Void> verifyPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        boolean ok = reviewService.verifyPassword(id, body.get("password"));
        return ok ? ResponseEntity.ok().build()
                  : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/{id}")
    public ReviewDetailDto updateReview(
            @PathVariable Long id,
            @RequestBody ReviewUpdateRequest request) {
        return ReviewDetailDto.from(reviewService.update(id, request));
    }

    @PostMapping("/{id}/done")
    public ResponseEntity<Void> markDone(@PathVariable Long id) {
        reviewService.markDone(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pending")
    public ResponseEntity<Void> markPending(@PathVariable Long id) {
        reviewService.markPending(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request) {
        reviewService.addComment(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request) {
        try {
            reviewService.updateComment(commentId, request.getPassword(), request.getContent());
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body) {
        try {
            reviewService.deleteComment(commentId, body.get("password"));
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/{id}/link")
    public ResponseEntity<?> getPortfolioLink(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String link = reviewService.getPortfolioLink(id, body.get("password"), body.get("adminKey"));
        if (link == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(Map.of("link", link));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok().build();
    }
}
