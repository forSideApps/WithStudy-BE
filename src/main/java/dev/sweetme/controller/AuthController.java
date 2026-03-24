package dev.sweetme.controller;

import dev.sweetme.domain.Member;
import dev.sweetme.domain.enums.MemberRole;
import dev.sweetme.domain.RoomApplication;
import dev.sweetme.dto.response.PostSummaryDto;
import dev.sweetme.dto.response.ReviewSummaryDto;
import dev.sweetme.dto.response.RoomSummaryDto;
import dev.sweetme.domain.Review;
import dev.sweetme.domain.ReviewExchange;
import dev.sweetme.repository.CommunityPostRepository;
import dev.sweetme.repository.MemberRepository;
import dev.sweetme.repository.ReviewExchangeRepository;
import dev.sweetme.repository.ReviewRepository;
import dev.sweetme.repository.RoomApplicationRepository;
import dev.sweetme.repository.RoomRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final ReviewRepository reviewRepository;
    private final CommunityPostRepository communityPostRepository;
    private final RoomApplicationRepository roomApplicationRepository;
    private final ReviewExchangeRepository reviewExchangeRepository;

    @Value("${app.oci.namespace}") private String ociNamespace;
    @Value("${app.oci.bucket}") private String ociBucket;
    @Value("${app.oci.region}") private String ociRegion;

    private String logoBaseUrl() {
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/SweetMe/", ociRegion, ociNamespace, ociBucket);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (memberRepository.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body(Map.of("message", "이미 사용 중인 아이디입니다."));
        }
        String email = (req.email() != null && !req.email().isBlank()) ? req.email().trim() : null;
        if (email != null && memberRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "이미 사용 중인 이메일입니다."));
        }
        memberRepository.save(Member.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .email(email)
                .role(MemberRole.USER)
                .jobRole(req.jobRole())
                .careerLevel(req.careerLevel())
                .algoGrade(req.algoGrade())
                .build());
        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpRequest) {
        Member member = memberRepository.findByUsername(req.username()).orElse(null);
        if (member == null || !passwordEncoder.matches(req.password(), member.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }
        // 세션 고정 공격 방지: 기존 세션 무효화 후 새 세션 생성
        HttpSession old = httpRequest.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("member_username", member.getUsername());
        session.setAttribute("member_role", member.getRole().name());
        return ResponseEntity.ok(new MeResponse(
                member.getUsername(), member.getRole().name(), member.getEmail(),
                member.getJobRole(), member.getCareerLevel(), member.getAlgoGrade()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String username = session != null ? (String) session.getAttribute("member_username") : null;
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        return memberRepository.findByUsername(username)
                .<ResponseEntity<?>>map(m -> ResponseEntity.ok(new MeResponse(
                        m.getUsername(), m.getRole().name(), m.getEmail(),
                        m.getJobRole(), m.getCareerLevel(), m.getAlgoGrade())))
                .orElse(ResponseEntity.status(401).body(Map.of("message", "사용자를 찾을 수 없습니다.")));
    }

    @GetMapping("/me/rooms")
    public ResponseEntity<?> myRooms(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String username = session != null ? (String) session.getAttribute("member_username") : null;
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        List<RoomSummaryDto> rooms = roomRepository.findByMemberUsernameOrderByCreatedAtDesc(username)
                .stream().map(r -> RoomSummaryDto.from(r, logoBaseUrl())).toList();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/me/reviews")
    public ResponseEntity<?> myReviews(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String username = session != null ? (String) session.getAttribute("member_username") : null;
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        List<ReviewSummaryDto> reviews = reviewRepository.findByMemberUsernameOrderByCreatedAtDesc(username)
                .stream().map(ReviewSummaryDto::from).toList();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/me/applications")
    public ResponseEntity<?> myApplications(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String username = session != null ? (String) session.getAttribute("member_username") : null;
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        List<MyApplicationDto> apps = roomApplicationRepository.findByMemberUsernameOrderByCreatedAtDesc(username)
                .stream().map(a -> new MyApplicationDto(
                        a.getId(),
                        a.getRoom().getId(),
                        a.getRoom().getTitle(),
                        a.getRoom().getCompany().getName(),
                        a.getStatus().name(),
                        a.getStatus().getDisplayName(),
                        a.getCreatedAt()
                )).toList();
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/me/posts")
    public ResponseEntity<?> myPosts(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String username = session != null ? (String) session.getAttribute("member_username") : null;
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        List<PostSummaryDto> posts = communityPostRepository.findByMemberUsernameOrderByCreatedAtDesc(username)
                .stream().map(PostSummaryDto::from).toList();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/me/exchanges")
    public ResponseEntity<?> myExchanges(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String username = session != null ? (String) session.getAttribute("member_username") : null;
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));

        List<Long> myReviewIds = reviewRepository.findByMemberUsernameOrderByCreatedAtDesc(username)
                .stream().map(Review::getId).toList();
        if (myReviewIds.isEmpty()) return ResponseEntity.ok(List.of());

        // 내 글에 서로보기 요청이 온 것 (내 글이 target)
        List<ReviewExchange> received = reviewExchangeRepository.findByTargetReviewIdIn(myReviewIds);
        // 내가 서로보기 요청한 것 (내 글이 requester)
        List<ReviewExchange> sent = reviewExchangeRepository.findByRequesterReviewIdIn(myReviewIds);

        List<Long> allReviewIds = new java.util.ArrayList<>();
        received.forEach(e -> { allReviewIds.add(e.getRequesterReviewId()); allReviewIds.add(e.getTargetReviewId()); });
        sent.forEach(e -> { allReviewIds.add(e.getRequesterReviewId()); allReviewIds.add(e.getTargetReviewId()); });

        Map<Long, Review> reviewMap = reviewRepository.findAllById(allReviewIds)
                .stream().collect(java.util.stream.Collectors.toMap(Review::getId, r -> r));

        List<ExchangeDto> result = new java.util.ArrayList<>();
        received.forEach(e -> {
            Review myReview = reviewMap.get(e.getTargetReviewId());
            Review theirReview = reviewMap.get(e.getRequesterReviewId());
            if (myReview != null && theirReview != null) {
                result.add(new ExchangeDto(e.getId(), "RECEIVED",
                        myReview.getId(), myReview.getTitle(),
                        theirReview.getId(), theirReview.getTitle(),
                        theirReview.getMemberUsername(), e.getCreatedAt(),
                        e.getStatus().name()));
            }
        });
        sent.forEach(e -> {
            Review myReview = reviewMap.get(e.getRequesterReviewId());
            Review theirReview = reviewMap.get(e.getTargetReviewId());
            if (myReview != null && theirReview != null) {
                result.add(new ExchangeDto(e.getId(), "SENT",
                        myReview.getId(), myReview.getTitle(),
                        theirReview.getId(), theirReview.getTitle(),
                        theirReview.getMemberUsername(), e.getCreatedAt(),
                        e.getStatus().name()));
            }
        });
        result.sort(java.util.Comparator.comparing(ExchangeDto::createdAt).reversed());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequest req, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        String username = session != null ? (String) session.getAttribute("member_username") : null;
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        memberRepository.findByUsername(username).ifPresent(m -> m.updateProfile(
                req.jobRole(), req.careerLevel(), req.algoGrade()));
        return ResponseEntity.ok(Map.of("message", "프로필이 업데이트되었습니다."));
    }

    public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 4, max = 100) String password,
        @NotBlank String email,
        String jobRole,
        String careerLevel,
        String algoGrade
    ) {}

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record ProfileRequest(String jobRole, String careerLevel, String algoGrade) {}

    public record MeResponse(
        String username, String role, String email,
        String jobRole, String careerLevel, String algoGrade
    ) {}

    public record MyApplicationDto(
        Long id, Long roomId, String roomTitle, String themeName,
        String status, String statusDisplay, java.time.LocalDateTime createdAt
    ) {}

    public record ExchangeDto(
        Long id, String direction,
        Long myReviewId, String myReviewTitle,
        Long theirReviewId, String theirReviewTitle,
        String theirUsername, java.time.LocalDateTime createdAt,
        String status
    ) {}
}
