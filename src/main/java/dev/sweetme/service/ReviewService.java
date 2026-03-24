package dev.sweetme.service;

import dev.sweetme.domain.Review;
import dev.sweetme.domain.enums.CareerLevel;
import dev.sweetme.domain.enums.ExchangeStatus;
import dev.sweetme.domain.enums.ReviewJobCategory;
import dev.sweetme.domain.enums.ReviewStatus;
import dev.sweetme.domain.enums.ReviewType;
import dev.sweetme.domain.ReviewComment;
import dev.sweetme.dto.CommentRequest;
import dev.sweetme.dto.ReviewRequest;
import dev.sweetme.dto.ReviewUpdateRequest;
import dev.sweetme.domain.ReviewExchange;
import dev.sweetme.repository.ReviewCommentRepository;
import dev.sweetme.repository.ReviewExchangeRepository;
import dev.sweetme.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewExchangeRepository exchangeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.page.review-size:15}")
    private int pageSize;

    public Page<Review> findReviews(String type, String status, String jobCategory, String careerLevel, String keyword, int page) {
        var pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        ReviewType t = parseEnum(type, ReviewType.class);
        ReviewStatus s = parseEnum(status, ReviewStatus.class);
        ReviewJobCategory jc = parseEnum(jobCategory, ReviewJobCategory.class);
        CareerLevel cl = parseEnum(careerLevel, CareerLevel.class);
        String kw = parseKeyword(keyword);
        return reviewRepository.search(t, s, jc, cl, kw, pageable);
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public Review create(ReviewRequest request, String memberUsername) {
        String hash = (request.getPassword() != null && !request.getPassword().isBlank())
                ? passwordEncoder.encode(request.getPassword()) : null;
        Review review = Review.builder()
                .type(ReviewType.valueOf(request.getType()))
                .jobCategory(ReviewJobCategory.valueOf(request.getJobCategory()))
                .careerLevel(CareerLevel.valueOf(request.getCareerLevel()))
                .title(request.getTitle())
                .content(request.getContent())
                .authorName(request.getAuthorName())
                .contactInfo(request.getContactInfo())
                .portfolioLink(request.getPortfolioLink())
                .passwordHash(hash)
                .memberUsername(memberUsername)
                .build();
        return reviewRepository.save(review);
    }

    public boolean verifyPassword(Long id, String rawPassword) {
        String hash = findById(id).getPasswordHash();
        if (hash == null) return false;
        return passwordEncoder.matches(rawPassword, hash);
    }

    public String getPortfolioLink(Long id, String rawPassword, boolean isAdmin, String memberUsername) {
        Review review = findById(id);
        boolean isOwner = memberUsername != null && memberUsername.equals(review.getMemberUsername());
        boolean hasExchangeAccess = memberUsername != null &&
                (exchangeRepository.hasAccessToRequesterLink(id, memberUsername)
                 || exchangeRepository.hasAccessAsRequester(id, memberUsername));
        if (!isAdmin && !isOwner && !hasExchangeAccess) {
            if (review.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, review.getPasswordHash())) {
                throw new SecurityException("비밀번호가 올바르지 않습니다.");
            }
        }
        return review.getPortfolioLink();
    }

    /** 서로보기 신청: PENDING 상태로 요청 생성 */
    @Transactional
    public void createExchangeRequest(Long targetReviewId, Long myReviewId, String sessionUsername) {
        Review myReview = findById(myReviewId);
        if (!sessionUsername.equals(myReview.getMemberUsername())) {
            throw new SecurityException("본인의 글만 제공할 수 있습니다.");
        }
        Review targetReview = findById(targetReviewId);
        if (sessionUsername.equals(targetReview.getMemberUsername())) {
            throw new IllegalArgumentException("자신의 글과는 교환할 수 없습니다.");
        }
        if (exchangeRepository.existsByRequesterReviewIdAndTargetReviewId(myReviewId, targetReviewId)) {
            throw new IllegalArgumentException("이미 서로보기 요청을 보냈습니다.");
        }
        exchangeRepository.save(ReviewExchange.builder()
                .requesterReviewId(myReviewId)
                .targetReviewId(targetReviewId)
                .build());
    }

    /** 서로보기 수락: target 리뷰 소유자만 가능 */
    @Transactional
    public void acceptExchange(Long exchangeId, String username) {
        ReviewExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("서로보기 요청을 찾을 수 없습니다."));
        Review targetReview = findById(exchange.getTargetReviewId());
        if (!username.equals(targetReview.getMemberUsername())) {
            throw new SecurityException("수락 권한이 없습니다.");
        }
        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        exchange.accept();
    }

    /** 서로보기 거절: 이력 보존을 위해 REJECTED 상태로 변경 */
    @Transactional
    public void rejectExchange(Long exchangeId, String username) {
        ReviewExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("서로보기 요청을 찾을 수 없습니다."));
        Review targetReview = findById(exchange.getTargetReviewId());
        if (!username.equals(targetReview.getMemberUsername())) {
            throw new SecurityException("거절 권한이 없습니다.");
        }
        exchange.reject();
    }

    @Transactional
    public Review update(Long id, ReviewUpdateRequest request) {
        Review review = findById(id);
        if (review.getStatus() == dev.sweetme.domain.enums.ReviewStatus.DONE) {
            throw new IllegalStateException("검토가 완료된 글은 수정할 수 없습니다.");
        }
        review.update(
                ReviewType.valueOf(request.getType()),
                ReviewJobCategory.valueOf(request.getJobCategory()),
                CareerLevel.valueOf(request.getCareerLevel()),
                request.getTitle(),
                request.getContent(),
                request.getContactInfo(),
                request.getPortfolioLink()
        );
        return review;
    }

    @Transactional
    public void markDone(Long id) {
        findById(id).markDone();
    }

    @Transactional
    public void markPending(Long id) {
        findById(id).markPending();
    }

    @Transactional
    public void incrementView(Long id) {
        findById(id).incrementViewCount();
    }

    @Transactional
    public void addComment(Long reviewId, CommentRequest request, boolean isAdmin, String memberUsername) {
        Review review = findById(reviewId);
        ReviewComment comment = ReviewComment.builder()
                .review(review)
                .authorName(isAdmin ? "운영자" : request.getAuthorName())
                .content(request.getContent())
                .isAdmin(isAdmin)
                .memberUsername(isAdmin ? null : memberUsername)
                .build();
        reviewCommentRepository.save(comment);
    }

    @Transactional
    public void updateComment(Long commentId, String content, boolean isAdmin, String memberUsername) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!isAdmin) {
            if (memberUsername == null || !memberUsername.equals(comment.getMemberUsername())) {
                throw new SecurityException("수정 권한이 없습니다.");
            }
        }
        comment.updateContent(content);
    }

    @Transactional
    public void deleteComment(Long commentId, boolean isAdmin, String memberUsername) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!isAdmin) {
            if (memberUsername == null || !memberUsername.equals(comment.getMemberUsername())) {
                throw new SecurityException("삭제 권한이 없습니다.");
            }
        }
        reviewCommentRepository.delete(comment);
    }

    public boolean isOwner(Long reviewId, String username) {
        if (username == null) return false;
        return username.equals(findById(reviewId).getMemberUsername());
    }

    @Transactional
    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> clazz) {
        return (value != null && !value.isBlank()) ? Enum.valueOf(clazz, value) : null;
    }

    private String parseKeyword(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }
}
