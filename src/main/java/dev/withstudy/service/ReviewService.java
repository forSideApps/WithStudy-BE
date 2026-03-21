package dev.withstudy.service;

import dev.withstudy.domain.Review;
import dev.withstudy.domain.enums.CareerLevel;
import dev.withstudy.domain.enums.ReviewJobCategory;
import dev.withstudy.domain.enums.ReviewType;
import dev.withstudy.domain.ReviewComment;
import dev.withstudy.dto.CommentRequest;
import dev.withstudy.dto.ReviewRequest;
import dev.withstudy.dto.ReviewUpdateRequest;
import dev.withstudy.repository.ReviewCommentRepository;
import dev.withstudy.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<Review> findReviews(String type, String jobCategory, String careerLevel, String keyword, int page) {
        var pageable = PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, "createdAt"));
        ReviewType t = parseEnum(type, ReviewType.class);
        ReviewJobCategory jc = parseEnum(jobCategory, ReviewJobCategory.class);
        CareerLevel cl = parseEnum(careerLevel, CareerLevel.class);
        String kw = parseKeyword(keyword);
        return reviewRepository.search(t, jc, cl, kw, pageable);
    }

    @Transactional(readOnly = true)
    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public Review create(ReviewRequest request) {
        Review review = Review.builder()
                .type(ReviewType.valueOf(request.getType()))
                .jobCategory(ReviewJobCategory.valueOf(request.getJobCategory()))
                .careerLevel(CareerLevel.valueOf(request.getCareerLevel()))
                .title(request.getTitle())
                .content(request.getContent())
                .authorName(request.getAuthorName())
                .contactInfo(request.getContactInfo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        return reviewRepository.save(review);
    }

    public boolean verifyPassword(Long id, String rawPassword) {
        return passwordEncoder.matches(rawPassword, findById(id).getPasswordHash());
    }

    @Transactional
    public Review update(Long id, ReviewUpdateRequest request) {
        Review review = findById(id);
        review.update(
                ReviewType.valueOf(request.getType()),
                ReviewJobCategory.valueOf(request.getJobCategory()),
                CareerLevel.valueOf(request.getCareerLevel()),
                request.getTitle(),
                request.getContent(),
                request.getContactInfo()
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
    public void addComment(Long reviewId, CommentRequest request) {
        Review review = findById(reviewId);
        ReviewComment comment = ReviewComment.builder()
                .review(review)
                .authorName(request.getAuthorName())
                .content(request.getContent())
                .build();
        reviewCommentRepository.save(comment);
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
