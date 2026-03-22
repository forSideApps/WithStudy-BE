package dev.sweetme.controller;

import dev.sweetme.domain.Member;
import dev.sweetme.domain.enums.MemberRole;
import dev.sweetme.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        Member member = memberRepository.findByUsername(req.username()).orElse(null);
        if (member == null || !passwordEncoder.matches(req.password(), member.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }
        session.setAttribute("member_username", member.getUsername());
        session.setAttribute("member_role", member.getRole().name());
        return ResponseEntity.ok(new MeResponse(
                member.getUsername(), member.getRole().name(), member.getEmail(),
                member.getJobRole(), member.getCareerLevel(), member.getAlgoGrade()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        String username = (String) session.getAttribute("member_username");
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        return memberRepository.findByUsername(username)
                .<ResponseEntity<?>>map(m -> ResponseEntity.ok(new MeResponse(
                        m.getUsername(), m.getRole().name(), m.getEmail(),
                        m.getJobRole(), m.getCareerLevel(), m.getAlgoGrade())))
                .orElse(ResponseEntity.status(401).body(Map.of("message", "사용자를 찾을 수 없습니다.")));
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequest req, HttpSession session) {
        String username = (String) session.getAttribute("member_username");
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
}
