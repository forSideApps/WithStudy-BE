package dev.sweetme.config;

import dev.sweetme.domain.Company;
import dev.sweetme.domain.Member;
import dev.sweetme.domain.enums.MemberRole;
import dev.sweetme.repository.CompanyRepository;
import dev.sweetme.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // admin 계정 초기화
        if (!memberRepository.existsByUsername("admin")) {
            memberRepository.save(Member.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin1234"))
                    .email(null)
                    .role(MemberRole.ADMIN)
                    .build());
            log.info("admin 계정 초기화 완료");
        }

        // 회사 초기화
        if (companyRepository.count() == 0) {
            List<Company> companies = List.of(
                Company.builder().name("소마 (소프트웨어 마에스트로)").slug("soma").accentColor("#4A90D9").displayOrder(1).build(),
                Company.builder().name("삼성 SW").slug("samsung").accentColor("#1428A0").displayOrder(2).build(),
                Company.builder().name("LG CNS / LG전자").slug("lg").accentColor("#A50034").displayOrder(3).build(),
                Company.builder().name("현대차 / 기아").slug("hyundai").accentColor("#002C5F").displayOrder(4).build(),
                Company.builder().name("카카오").slug("kakao").accentColor("#FEE500").displayOrder(5).build(),
                Company.builder().name("네이버").slug("naver").accentColor("#03C75A").displayOrder(6).build(),
                Company.builder().name("라인").slug("line").accentColor("#00B900").displayOrder(7).build(),
                Company.builder().name("쿠팡").slug("coupang").accentColor("#EE2222").displayOrder(8).build(),
                Company.builder().name("토스 / 핀테크").slug("toss").accentColor("#0064FF").displayOrder(9).build(),
                Company.builder().name("스타트업 / 기타").slug("startup").accentColor("#7C3AED").displayOrder(10).build()
            );
            companyRepository.saveAll(companies);
            log.info("회사 데이터 초기화 완료: {}개", companies.size());
        }
    }
}
