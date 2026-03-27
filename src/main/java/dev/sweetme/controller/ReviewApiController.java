package dev.sweetme.controller;

import dev.sweetme.dto.CommentRequest;
import dev.sweetme.dto.CommentUpdateRequest;
import dev.sweetme.dto.ReviewRequest;
import dev.sweetme.dto.ReviewUpdateRequest;
import dev.sweetme.dto.response.ReviewDetailDto;
import dev.sweetme.dto.response.ReviewSummaryDto;
import dev.sweetme.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController extends BaseApiController {

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
    public Map<String, Long> createReview(@Valid @RequestBody ReviewRequest request, HttpServletRequest httpRequest) {
        String memberUsername = getSessionUsername(httpRequest);
        if (memberUsername != null) {
            request.setAuthorName(memberUsername);
        }
        var review = reviewService.create(request, memberUsername);
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
    public ResponseEntity<Void> markDone(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        reviewService.markDone(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pending")
    public ResponseEntity<Void> markPending(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        reviewService.markPending(id);
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
        reviewService.addComment(id, request, isAdmin(httpRequest), memberUsername);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            reviewService.updateComment(commentId, request.getContent(), isAdmin(httpRequest), getSessionUsername(httpRequest));
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {
        try {
            reviewService.deleteComment(commentId, isAdmin(httpRequest), getSessionUsername(httpRequest));
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/link")
    public ResponseEntity<?> getPortfolioLink(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {
        try {
            String link = reviewService.getPortfolioLink(id, body.get("password"), isAdmin(httpRequest), getSessionUsername(httpRequest));
            if (link == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            return ResponseEntity.ok(Map.of("link", link));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/{id}/exchange")
    public ResponseEntity<?> exchange(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            HttpServletRequest httpRequest) {
        String username = getSessionUsername(httpRequest);
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        try {
            Long myReviewId = body.get("myReviewId");
            if (myReviewId == null) return ResponseEntity.badRequest().body(Map.of("message", "내 글을 선택해주세요."));
            reviewService.createExchangeRequest(id, myReviewId, username);
            return ResponseEntity.ok(Map.of("message", "서로보기 요청이 전송되었습니다. 상대방이 수락하면 링크를 확인할 수 있습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/exchanges/{exchangeId}/accept")
    public ResponseEntity<?> acceptExchange(@PathVariable Long exchangeId, HttpServletRequest request) {
        String username = getSessionUsername(request);
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        try {
            reviewService.acceptExchange(exchangeId, username);
            return ResponseEntity.ok(Map.of("message", "서로보기 요청을 수락했습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/exchanges/{exchangeId}/reject")
    public ResponseEntity<?> rejectExchange(@PathVariable Long exchangeId, HttpServletRequest request) {
        String username = getSessionUsername(request);
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        try {
            reviewService.rejectExchange(exchangeId, username);
            return ResponseEntity.ok(Map.of("message", "서로보기 요청을 거절했습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/exchanges/{exchangeId}")
    public ResponseEntity<?> cancelExchange(@PathVariable Long exchangeId, HttpServletRequest request) {
        String username = getSessionUsername(request);
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        try {
            reviewService.cancelExchange(exchangeId, username);
            return ResponseEntity.ok(Map.of("message", "서로보기 요청을 취소했습니다."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, HttpServletRequest request) {
        String username = getSessionUsername(request);
        if (!isAdmin(request) && !reviewService.isOwner(id, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        reviewService.delete(id);
        return ResponseEntity.ok().build();
    }

}
