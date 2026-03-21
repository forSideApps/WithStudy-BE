package dev.withstudy.controller;

import dev.withstudy.dto.CommentRequest;
import dev.withstudy.dto.ReviewRequest;
import dev.withstudy.dto.ReviewUpdateRequest;
import dev.withstudy.dto.response.ReviewDetailDto;
import dev.withstudy.dto.response.ReviewSummaryDto;
import dev.withstudy.service.ReviewService;
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
            @RequestParam(required = false) String jobCategory,
            @RequestParam(required = false) String careerLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return reviewService.findReviews(type, jobCategory, careerLevel, keyword, page)
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok().build();
    }
}
