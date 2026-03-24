package dev.sweetme.config;

import dev.sweetme.domain.Company;
import dev.sweetme.domain.CommunityComment;
import dev.sweetme.domain.CommunityPost;
import dev.sweetme.domain.Member;
import dev.sweetme.domain.Room;
import dev.sweetme.domain.enums.JobRole;
import dev.sweetme.domain.enums.MemberRole;
import dev.sweetme.domain.enums.PostCategory;
import dev.sweetme.repository.CompanyRepository;
import dev.sweetme.repository.CommunityCommentRepository;
import dev.sweetme.repository.CommunityPostRepository;
import dev.sweetme.repository.MemberRepository;
import dev.sweetme.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CompanyRepository companyRepository;
    private final RoomRepository roomRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
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

        // 방 목데이터
        boolean roomNeedsReseed = roomRepository.count() == 0 ||
            roomRepository.findAll().stream().noneMatch(r -> "소마 15기 면접 집중 스터디".equals(r.getTitle()));
        if (roomNeedsReseed) {
            roomRepository.deleteAll();
            String pw = passwordEncoder.encode("1234");
            Map<String, Company> companyMap = new HashMap<>();
            companyRepository.findAll().forEach(c -> companyMap.put(c.getSlug(), c));

            List<Room> rooms = new ArrayList<>();
            rooms.add(room(companyMap.get("soma"),    "소마 15기 면접 집중 스터디",               "소프트웨어 마에스트로 15기를 목표로 모인 스터디입니다. 주 2회 모의 면접, CS 기초 정리, 포트폴리오 피드백을 함께 진행합니다.",   JobRole.BACKEND,  "박지훈",  pw, "https://open.kakao.com/mock1",   5));
            rooms.add(room(companyMap.get("samsung"), "삼성 SW역량테스트 B형 대비",               "삼성 SW 역량 테스트 B형 합격을 목표로 알고리즘 문제를 함께 풀고 코드 리뷰합니다. 주 3회 문제 풀이 후 해설 공유.",             JobRole.BACKEND,  "이서연",  pw, "https://open.kakao.com/mock2",   6));
            rooms.add(room(companyMap.get("kakao"),   "카카오 공채 코딩테스트 준비",               "카카오 공채 기출 문제 풀이 및 유형 분석 스터디입니다. 프로그래머스 고득점 Kit 완주를 목표로 매일 1~2문제 풀고 리뷰합니다.",      JobRole.BACKEND,  "정현우",  pw, "https://open.kakao.com/mock3",   5));
            rooms.add(room(companyMap.get("naver"),   "네이버 백엔드 기술 면접 스터디",             "네이버 공채 백엔드 직군 기술 면접 대비 스터디입니다. 자료구조, OS, 네트워크, DB, Spring 심화까지 체계적으로 준비합니다.",       JobRole.BACKEND,  "최예진",  pw, "https://open.kakao.com/mock4",   4));
            rooms.add(room(companyMap.get("kakao"),   "카카오 프론트엔드 기술 면접 대비",           "React, JavaScript 심화 및 브라우저 동작 원리, 성능 최적화 등 카카오 FE 면접을 집중 준비합니다. 주 2회 모의 면접 진행.",      JobRole.FRONTEND, "한승민",  pw, "https://open.kakao.com/mock5",   4));
            rooms.add(room(companyMap.get("toss"),    "토스 챕터 면접 준비 스터디",                "토스 채용 프로세스 특성에 맞춰 챕터 인터뷰를 집중 대비합니다. 실제 토스 출제 스타일 기반 모의 면접을 주 2회 진행합니다.",          JobRole.BACKEND,  "강수빈",  pw, "https://open.kakao.com/mock6",   4));
            rooms.add(room(companyMap.get("lg"),      "LG CNS 신입 공채 코딩테스트 대비",          "LG CNS 신입 공채 코딩테스트 준비 스터디입니다. 최신 기출 분석과 함께 알고리즘 핵심 유형을 효율적으로 학습합니다.",              JobRole.BACKEND,  "윤지아",  pw, "https://open.kakao.com/mock7",   5));
            rooms.add(room(companyMap.get("hyundai"), "현대차 소프트웨어 직군 면접 대비",           "현대차그룹 소프트웨어 개발 직군 코딩테스트 및 기술 면접을 함께 준비합니다. 임베디드/SW 모두 환영합니다.",                       JobRole.BACKEND,  "임도현",  pw, "https://open.kakao.com/mock8",   6));
            rooms.add(room(companyMap.get("naver"),   "네이버 FE · 풀스택 취준 스터디",            "네이버 FE 및 풀스택 직군 지원자들의 포트폴리오 점검 및 기술 면접 준비 스터디입니다. 코드 리뷰와 모의 면접을 병행합니다.",         JobRole.FRONTEND, "신지우",  pw, "https://open.kakao.com/mock9",   5));
            rooms.add(room(companyMap.get("coupang"), "쿠팡 개발 직군 코테 · 시스템 디자인",       "쿠팡 개발 직군 코딩테스트 및 시스템 디자인 인터뷰를 준비합니다. 이커머스 도메인 특화 설계 문제를 중점적으로 다룹니다.",            JobRole.BACKEND,  "오채원",  pw, "https://open.kakao.com/mock10",  5));
            roomRepository.saveAll(rooms);
            log.info("방 목데이터 초기화 완료: {}개", rooms.size());
        }

        // 커뮤니티 목데이터 — 조회수·댓글 포함 (항상 초기화)
        {
            communityPostRepository.deleteAll();

            List<Object[]> postData = List.of(
                new Object[]{PostCategory.FREE, "카카오 1차 코테 후기 공유합니다",
                    "오늘 카카오 1차 코딩테스트 봤는데 생각보다 그래프 문제가 엄청 많이 나왔어요!! BFS/DFS 확실히 챙기세요 ㅎㅎ 저는 5문제 중 4문제 풀었는데 통과할 수 있을지 모르겠네요 ㅠㅠ 시간이 너무 촉박해서 마지막 문제는 아이디어만 짜다가 끝났어요... 다들 코테 결과 어떠셨나요??",
                    "코딩하는감자", LocalDateTime.of(2025,3,8,14,23,7), 312,
                    new Object[][]{
                        {"알고리즘마니아", "저도 오늘 봤는데 그래프 + DP 조합 문제가 진짜 까다로웠어요 ㅠㅠ 4/5 풀었는데 합격 컷이 어느 정도일지 감이 안 와요!", LocalDateTime.of(2025,3,8,17,11,42)},
                        {"취준하는곰", "BFS/DFS는 기본 중의 기본이죠 ㅎㅎ 저는 삼성 준비할 때 백준 골드 이상 그래프 문제를 200개 정도 풀었어요. 카카오도 결국 기본기가 중요한 것 같아요!", LocalDateTime.of(2025,3,8,19,55,18)},
                        {"면접공포증", "후기 감사해요!! 저는 다음 달 원서 쓸 건데 미리 준비해야겠네요 ㅎㅎ 혹시 준비 기간이 어느 정도 됐나요?", LocalDateTime.of(2025,3,9,9,3,5)},
                        {"코딩하는감자", "@면접공포증 저는 약 3개월 준비했어요! 프로그래머스 고득점 kit 전부 풀고 기출 3년치 돌렸습니다 ㅎㅎ", LocalDateTime.of(2025,3,9,14,27,33)},
                    }},
                new Object[]{PostCategory.FREE, "네이버 면접 준비하면서 느낀 점",
                    "네이버 기술 면접은 정말 깊게 파고드는 것 같아요 ㄷㄷ 특히 JVM 동작 원리, GC 알고리즘, Java 동시성 처리 관련 질문이 엄청 많았어요. 단순히 \"GC가 뭔가요?\" 수준이 아니라 G1GC와 ZGC 차이라든지 STW 최소화 방법까지 물어보더라고요... OS, 네트워크 기본기도 탄탄해야 합니다!!",
                    "백엔드만세", LocalDateTime.of(2025,3,14,9,45,33), 487,
                    new Object[][]{
                        {"프론트엔드러", "네이버 면접 그렇게 깊게 보는군요 ㄷㄷ CS 기본기 다시 닦아야겠어요... 혹시 참고하신 책이나 자료 있으신가요?", LocalDateTime.of(2025,3,14,12,8,17)},
                        {"백엔드만세", "@프론트엔드러 '개발자가 반드시 알아야 할 자바 성능 튜닝 이야기' 강추예요!! JVM 내부 동작을 이해하는 데 정말 좋았어요 ㅎㅎ", LocalDateTime.of(2025,3,14,15,44,2)},
                        {"밤샘개발자", "저도 네이버 1차 통과하고 2차 면접 준비 중인데 덕분에 방향 잡겠어요! 현직자분이 쓰신 글인가요?", LocalDateTime.of(2025,3,15,8,19,51)},
                        {"백엔드만세", "@밤샘개발자 네, 저번 달에 최종 합격했어요 ㅎㅎㅎ 2차는 코딩 테스트도 한 번 더 있으니까 그것도 꼭 준비하세요!", LocalDateTime.of(2025,3,15,11,36,28)},
                        {"코테통과기원", "실제 합격하신 분 후기라니 너무 귀한 정보예요!! 감사합니다 ㅎㅎ", LocalDateTime.of(2025,3,15,22,4,9)},
                    }},
                new Object[]{PostCategory.FREE, "토스 서류 합격 후 코테 준비 팁",
                    "토스 서류 합격했습니다!!! 같이 코테 준비할 분들 모여요 ㅎㅎ 들은 바로는 구현 문제 위주에 시스템 설계 감각도 본다고 하더라고요. LeetCode Medium~Hard 위주로 풀고 있는데 혼자라 외롭네요 ㅋㅋㅋ 다들 코테 어떻게 준비하세요?",
                    "서류합격뚝딱", LocalDateTime.of(2025,3,21,20,12,55), 276,
                    new Object[][]{
                        {"백엔드지망생", "저도 서류 합격이요!! 같이 해요 ㅎㅎㅎ 저는 주로 그리디 + DP 쪽 집중하고 있어요!", LocalDateTime.of(2025,3,21,22,47,14)},
                        {"깃허브초보", "토스 코테 작년에 봤는데 구현 비중이 높고 시간이 진짜 빡빡했어요 ㅠㅠ 빠른 입출력 꼭 챙기세요!", LocalDateTime.of(2025,3,22,7,30,59)},
                        {"서류합격뚝딱", "@깃허브초보 작년 경험담이네요 ㅎㅎ 혹시 합격하셨나요?", LocalDateTime.of(2025,3,22,10,15,37)},
                        {"깃허브초보", "@서류합격뚝딱 네!! 현재 토스 재직 중이에요 ㅎㅎ 어려운 거 있으면 편하게 질문하세요~", LocalDateTime.of(2025,3,22,14,58,22)},
                    }},
                new Object[]{PostCategory.FREE, "삼성 SW 역량테스트 B형 자료 정리",
                    "B형 준비하면서 정리한 내용 공유합니다!!\n\n1. STL 없이 자료구조 직접 구현 (링크드리스트, 큐, 스택, 힙)\n2. 완전탐색 + 최적화가 핵심\n3. 시간복잡도 O(NlogN) 이내로 풀어야 함\n4. 삼성 SW Expert Academy 문제 최소 500개 이상 풀기\n\n특히 커스텀 정렬 구현이 단골로 나오니 꼭 연습하세요!!",
                    "삼성도전중", LocalDateTime.of(2025,3,28,16,38,22), 531,
                    new Object[][]{
                        {"개발공부중", "정리 감사해요!! B형 자체 구현 파트가 진짜 어렵더라고요 ㅠㅠ 혹시 몇 회 차에 합격하셨나요?", LocalDateTime.of(2025,3,28,19,2,44)},
                        {"삼성도전중", "@개발공부중 3회 차요 ㅠ 처음엔 STL에 너무 의존하다가 된통 혼났어요 ㅋㅋㅋ", LocalDateTime.of(2025,3,28,21,17,6)},
                        {"리트코더", "500개... 저는 아직 100개도 못 풀었는데 분발해야겠네요 ㅠㅠ", LocalDateTime.of(2025,3,29,8,44,31)},
                        {"합격기원해요", "직접 구현 연습이 핵심이군요 ㅎㅎ 저는 C++ 실력을 늘리는 게 먼저일 것 같아요!", LocalDateTime.of(2025,3,29,13,22,50)},
                        {"삼성도전중", "모두 화이팅입니다!! B형 붙으면 삼성 코테는 걱정 없어요 ㅎㅎㅎ", LocalDateTime.of(2025,3,30,9,5,17)},
                    }},
                new Object[]{PostCategory.FREE, "소마 15기 최종 합격 후기",
                    "소마 15기 최종 합격했습니다!!! 면접에서는 프로젝트를 정말 깊게 파고들었어요. \"왜 이 기술 스택을 선택했나요?\", \"이 부분을 다시 만든다면 어떻게 바꾸겠어요?\" 같은 질문이 많았습니다. 기술 선택의 이유와 트레이드오프를 명확하게 설명할 수 있도록 준비하세요!!",
                    "소마합격기원", LocalDateTime.of(2025,4,4,11,5,44), 842,
                    new Object[][]{
                        {"취준전사", "축하드려요!!! 저도 15기 도전할 건데 포폴 어떻게 구성하셨어요??", LocalDateTime.of(2025,4,4,13,41,8)},
                        {"소마합격기원", "@취준전사 저는 개인 프로젝트 2개 + 팀 프로젝트 1개로 구성했어요 ㅎㅎ 개인 프로젝트에서 기술적 깊이를 보여주는 게 중요한 것 같아요!", LocalDateTime.of(2025,4,4,16,28,55)},
                        {"알고리즘마니아", "소마 인기 많죠 ㄷㄷ 경쟁률이 어느 정도 됐나요?", LocalDateTime.of(2025,4,5,9,13,37)},
                        {"소마합격기원", "@알고리즘마니아 공개된 건 없지만 체감상 10~15:1 정도 되는 것 같았어요 ㅠㅠ", LocalDateTime.of(2025,4,5,11,47,19)},
                        {"코딩하는감자", "합격 진짜 축하해요!! 소마 생활도 후기 올려주세요~ 기대됩니다 ㅎㅎㅎ", LocalDateTime.of(2025,4,5,18,32,4)},
                        {"서류합격뚝딱", "좋은 후기 감사해요!! 저도 내년에 꼭 도전해봐야겠어요!", LocalDateTime.of(2025,4,6,10,6,48)},
                    }},
                new Object[]{PostCategory.FREE, "현대차 SW 직군 코테 난이도 어때요?",
                    "현대차 SW 직군 코테 준비 중인데 난이도나 출제 경향이 너무 궁금해요!! 알고리즘 위주인지, 구현 위주인지, SQL도 나오는지 아시는 분 계신가요? 취준 중인데 정보가 없어서 막막하네요 ㅠㅠ",
                    "현차뚫을거야", LocalDateTime.of(2025,4,11,22,51,9), 198,
                    new Object[][]{
                        {"면접공포증", "제가 작년에 봤는데 알고리즘 + 구현 혼합이었어요! 프로그래머스 레벨 3 수준이면 충분히 풀 수 있었어요. SQL은 없었어요 ㅎㅎ", LocalDateTime.of(2025,4,12,1,24,33)},
                        {"현차뚫을거야", "@면접공포증 감사해요!! 레벨 3 위주로 연습해볼게요 ㅎㅎ", LocalDateTime.of(2025,4,12,8,37,55)},
                        {"밤샘개발자", "저도 올 상반기에 보려고 준비 중이에요 ㅎㅎ 같이 스터디 하실 분 계세요?", LocalDateTime.of(2025,4,12,20,9,11)},
                    }},
                new Object[]{PostCategory.FREE, "LG CNS vs LG전자 어디가 나을까요?",
                    "LG CNS와 LG전자 SW 직군 둘 다 지원하려는데 고민이 너무 많아요 ㅠㅠ 개발 문화, 기술 스택, 워라밸 등 아시는 분들 정보 공유 부탁드려요!! 특히 신입 개발자 입장에서 성장하기 좋은 곳이 어딘지 궁금해요.",
                    "LG가즈아", LocalDateTime.of(2025,4,18,8,17,36), 356,
                    new Object[][]{
                        {"프론트엔드러", "LG CNS는 SI/SM 업무도 있어서 처음엔 고객사 파견이 많을 수 있어요. LG전자는 제품 SW 개발이라 도메인이 명확하고 성장 경로가 뚜렷한 편이에요 ㅎㅎ", LocalDateTime.of(2025,4,18,11,4,22)},
                        {"코테통과기원", "저는 LG CNS 3년차인데 요즘 클라우드/AI 전환 중이라 기술 스택이 많이 최신화되고 있어요!! 나쁘지 않아요 ㅎㅎ", LocalDateTime.of(2025,4,18,15,39,47)},
                        {"LG가즈아", "@코테통과기원 현직자 정보 감사합니다!! DX 전환 관련 프로젝트가 많나요?", LocalDateTime.of(2025,4,18,19,53,14)},
                        {"코테통과기원", "@LG가즈아 네, 요즘 AWS·Azure 기반 MSA 전환 프로젝트가 많아요 ㅎㅎ 클라우드 자격증 미리 따두면 진짜 도움돼요~", LocalDateTime.of(2025,4,19,9,17,38)},
                        {"취준하는곰", "두 곳 다 좋아 보이네요 ㅎㅎ 합격이 우선이겠지만 ㅋㅋㅋ 화이팅!!!", LocalDateTime.of(2025,4,19,14,42,5)},
                    }},
                new Object[]{PostCategory.FREE, "쿠팡 시스템 디자인 면접 준비 방법",
                    "쿠팡 2차 면접에서 시스템 디자인을 물어본다고 들었어요!! 어떻게 준비하셨나요?? 주로 어떤 주제가 나왔는지, 추천 자료도 같이 공유해주시면 정말 감사하겠어요 ㅎㅎ",
                    "시스템디자이너", LocalDateTime.of(2025,4,25,19,43,1), 423,
                    new Object[][]{
                        {"백엔드지망생", "'Designing Data-Intensive Applications' 책이 진짜 필독서예요!! 분산 시스템 이론이 잘 정리돼 있어요 ㅎㅎ", LocalDateTime.of(2025,4,25,22,11,29)},
                        {"개발공부중", "YouTube에 'System Design Interview' 채널 추천드려요! 실제 면접 시뮬레이션 형태로 나와서 감 잡기 진짜 좋아요 ㅎㅎㅎ", LocalDateTime.of(2025,4,26,8,54,43)},
                        {"시스템디자이너", "@백엔드지망생 @개발공부중 감사해요!! 두 가지 모두 바로 시작해볼게요 ㅎㅎ", LocalDateTime.of(2025,4,26,12,30,6)},
                        {"리트코더", "쿠팡은 특히 주문 처리 시스템이나 검색 인프라 같은 이커머스 도메인 시스템을 설계해보는 연습이 도움됐어요!!", LocalDateTime.of(2025,4,26,20,7,55)},
                        {"시스템디자이너", "이커머스 도메인 위주로 연습해봐야겠네요!! 다들 진짜 감사합니다 ㅎㅎ", LocalDateTime.of(2025,4,27,10,23,18)},
                    }},
                new Object[]{PostCategory.FREE, "취업 준비하면서 멘탈 관리 어떻게 하세요?",
                    "코테 계속 떨어지고 서류도 안 되고... 진짜 힘드네요 ㅠㅠ 다들 취준하면서 멘탈 어떻게 관리하세요? 포기하고 싶다는 생각이 들 때 어떻게 이겨내시는지 궁금해요. 그냥 공감받고 싶어서 글 써봅니다...",
                    "멘탈챙겨야지", LocalDateTime.of(2025,5,3,21,15,44), 267,
                    new Object[][]{
                        {"소마합격기원", "진짜 공감돼요 ㅠㅠ 저도 서류 10군데 다 떨어지고 나서 한 달 쉬었어요. 그 시간이 오히려 방향을 다시 잡는 데 도움이 됐어요.", LocalDateTime.of(2025,5,3,22,41,8)},
                        {"취준전사", "취준은 마라톤이에요 ㅠㅠ 조급해하지 말고 오늘 할 수 있는 것 하나씩만 하세요. 응원합니다!!", LocalDateTime.of(2025,5,4,8,17,33)},
                        {"백엔드만세", "저도 7번 떨어지고 8번째에 붙었어요. 포기하지 마세요!! 떨어지는 게 실력이 없어서가 아니라 타이밍이 안 맞는 경우도 정말 많아요.", LocalDateTime.of(2025,5,4,12,55,19)},
                        {"알고리즘마니아", "운동하면서 기분 전환하는 거 진짜 도움돼요 ㅎㅎ 하루 30분만 걸어도 머리가 맑아지더라고요. 화이팅!!", LocalDateTime.of(2025,5,4,19,3,47)},
                        {"멘탈챙겨야지", "모두 따뜻한 댓글 감사해요 ㅠㅠ 덕분에 다시 힘낼 수 있을 것 같아요. 다들도 취준 화이팅!!", LocalDateTime.of(2025,5,5,10,28,6)},
                    }},
                new Object[]{PostCategory.FREE, "스터디 짝꿍 구해요! 같이 PS 해요",
                    "혼자 알고리즘 공부하다 보니 너무 외롭고 동기부여가 안 돼서요 ㅠㅠ 같이 매일 1문제씩 풀고 서로 코드 리뷰해줄 짝꿍 구합니다! 레벨은 프로그래머스 레벨 2~3 수준이고, 언어는 Java 또는 Python 환영해요. 혹시 관심 있으신 분 댓글 달아주세요~",
                    "알고리즘입문자", LocalDateTime.of(2025,5,10,16,44,27), 143,
                    new Object[][]{
                        {"코딩하는감자", "저 할게요!! 저도 Java로 하고 있고 레벨 2~3 준비 중이에요 ㅎㅎ 카카오톡 아이디 공유해주세요~", LocalDateTime.of(2025,5,10,18,22,53)},
                        {"깃허브초보", "저도 참여하고 싶어요!! Python이어도 괜찮나요? 같이 리뷰하면서 서로 배울 수 있을 것 같아요 ㅎㅎ", LocalDateTime.of(2025,5,10,21,7,14)},
                        {"알고리즘입문자", "@코딩하는감자 @깃허브초보 저도 너무 반가워요!! 3명이서 소규모로 운영해봐요 ㅎㅎ 오픈채팅 링크 만들어서 공유할게요~", LocalDateTime.of(2025,5,11,9,15,38)},
                        {"리트코더", "저도 끼워주세요 ㅠㅠ Java 사용하고 레벨 3 준비 중이에요! 늦게 봤는데 아직 자리 있나요??", LocalDateTime.of(2025,5,11,14,49,2)},
                    }},
                new Object[]{PostCategory.SUGGESTION, "모집 마감 스터디 필터링 기능 요청",
                    "현재 전체/모집중 필터만 있는데, 마감된 스터디도 따로 볼 수 있는 필터가 있으면 좋겠어요 ㅎㅎ 과거 스터디 내용이나 커리큘럼을 참고하고 싶을 때 유용할 것 같아요!! 또한 종료된 스터디의 후기도 볼 수 있으면 정말 좋겠어요.",
                    "기능추가마법사", LocalDateTime.of(2025,3,20,15,29,48), 89,
                    new Object[][]{
                        {"합격기원해요", "저도 이 기능 필요하다고 생각했어요!! 특히 후기 기능이랑 연계되면 정말 좋을 것 같아요 ㅎㅎ", LocalDateTime.of(2025,3,20,18,14,22)},
                        {"취준전사", "+1 동의합니다!! 어떤 스터디가 실제로 잘 운영됐는지 알 수 있으면 선택에 진짜 도움이 될 것 같아요 ㅎㅎ", LocalDateTime.of(2025,3,21,9,37,45)},
                    }},
                new Object[]{PostCategory.SUGGESTION, "스터디 후기 작성 기능 추가 요청",
                    "스터디 종료 후 참여자들이 후기를 남길 수 있는 기능이 있으면 정말 좋겠어요 ㅎㅎ 어떤 스터디가 잘 운영됐는지, 스터디장이 어떤지 등을 다른 사람들도 참고할 수 있게요!! 별점 같은 평가 기능도 있으면 더 좋을 것 같아요.",
                    "후기남기고싶어", LocalDateTime.of(2025,4,2,10,55,17), 134,
                    new Object[][]{
                        {"알고리즘마니아", "완전 공감이에요!! 스터디 선택할 때 후기가 있으면 정말 도움될 것 같아요 ㅎㅎ", LocalDateTime.of(2025,4,2,13,22,41)},
                        {"소마합격기원", "별점 + 한줄 후기 정도만 있어도 충분할 것 같아요 ㅎㅎ 구현해주시면 진짜 자주 쓸게요!!", LocalDateTime.of(2025,4,2,19,48,3)},
                        {"후기남기고싶어", "많은 분들이 원하시는 기능 같으니 운영팀에서 빨리 반영해주시면 좋겠어요 ㅎㅎ!!", LocalDateTime.of(2025,4,3,8,34,57)},
                    }},
                new Object[]{PostCategory.SUGGESTION, "서로보기 요청 시 알림 기능 요청",
                    "포폴/이력서 서로보기 요청이 왔을 때 알림을 받을 수 있으면 좋겠어요 ㅠㅠ 매번 마이페이지 들어가서 확인하기 번거롭고, 요청이 와도 모르고 지나치는 경우가 있어요. 이메일 알림이나 푸시 알림 기능 추가 부탁드려요!",
                    "알림주세요제발", LocalDateTime.of(2025,5,1,13,21,59), 201,
                    new Object[][]{
                        {"취준하는곰", "이거 진짜 필요해요!! 요청이 왔는지 몰라서 좋은 기회를 놓친 적이 있어요 ㅠㅠ 꼭 추가해주세요!", LocalDateTime.of(2025,5,1,16,8,34)},
                        {"현차뚫을거야", "이메일 알림만 있어도 충분할 것 같아요 ㅎㅎ 구현 비용 대비 효용이 진짜 클 거예요!!", LocalDateTime.of(2025,5,2,9,45,12)},
                        {"백엔드만세", "저도 동의해요!! 이메일 알림 정도면 충분하고 빠르게 구현될 수 있을 것 같아요 ㅎㅎ", LocalDateTime.of(2025,5,2,18,22,47)},
                    }}
            );

            List<CommunityComment> allComments = new ArrayList<>();

            for (Object[] data : postData) {
                PostCategory category  = (PostCategory) data[0];
                String title           = (String) data[1];
                String content         = (String) data[2];
                String author          = (String) data[3];
                LocalDateTime postTime = (LocalDateTime) data[4];
                int viewCount          = (int) data[5];
                Object[][] commentData = (Object[][]) data[6];

                CommunityPost post = CommunityPost.builder()
                        .category(category)
                        .title(title)
                        .content(content)
                        .authorName(author)
                        .viewCount(viewCount)
                        .createdAt(postTime)
                        .build();
                communityPostRepository.save(post);

                for (Object[] c : commentData) {
                    allComments.add(CommunityComment.builder()
                            .post(post)
                            .authorName((String) c[0])
                            .content((String) c[1])
                            .createdAt((LocalDateTime) c[2])
                            .build());
                }
            }

            communityCommentRepository.saveAll(allComments);
            log.info("커뮤니티 데이터 초기화 완료 (댓글 포함): {}개 댓글", allComments.size());
        }
    }

    private Room room(Company company, String title, String desc, JobRole jobRole,
                      String nickname, String pw, String kakaoLink, int maxMembers) {
        return Room.builder()
                .company(company)
                .title(title)
                .description(desc)
                .maxMembers(maxMembers)
                .creatorNickname(nickname)
                .passwordHash(pw)
                .kakaoLink(kakaoLink)
                .jobRole(jobRole)
                .requirements("적극적으로 참여 가능한 분")
                .build();
    }
}
