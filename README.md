# 인터뷰스터디 🎯

면접 스터디를 구할 수 있는 매칭 플랫폼입니다.

## 주요 기능

- **테마별 스터디 방** - 소마, 삼성, LG, 현대, 카카오, 네이버 등 기업 테마별 스터디 방 개설/참여
- **신청 관리** - 방장이 신청자 목록을 검토하고 승인/거절
- **카카오톡 연결** - 승인된 스터디원에게 오픈채팅 링크 전달
- **커뮤니티 게시판** - 지역 정보, 건의사항, 자유게시판
- **회원가입 불필요** - 닉네임만으로 자유롭게 이용

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot 3.3, Spring MVC, Spring Data JPA |
| Frontend | Thymeleaf, Vanilla CSS (No Framework) |
| Database | Oracle DB 21c XE |
| Build | Gradle (Groovy DSL) |
| Deploy | Docker, OCI Compute + OCIR |
| CI/CD | GitHub Actions |

## 로컬 실행 (Docker)

```bash
# Oracle DB + App 한번에 실행
docker compose up -d

# 접속
open http://localhost:8080
```

Oracle DB 초기화에 약 1~2분이 소요됩니다.

## 로컬 실행 (개발)

사전에 Oracle DB가 실행 중이어야 합니다.

```bash
# 빌드
./gradlew bootJar

# 환경변수 설정
export DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
export DB_USERNAME=interview_user
export DB_PASSWORD=interview_pass

# 실행
java -jar build/libs/app.jar
```

## 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `DB_URL` | Oracle JDBC URL | `jdbc:oracle:thin:@localhost:1521/XEPDB1` |
| `DB_USERNAME` | DB 사용자 | `interview_user` |
| `DB_PASSWORD` | DB 비밀번호 | `interview_pass` |
| `DDL_AUTO` | JPA DDL 전략 | `update` |
| `SERVER_PORT` | 서버 포트 | `8080` |

## GitHub Actions CI/CD (OCI)

`.github/workflows/ci-cd.yml` 실행을 위해 아래 Secrets 설정이 필요합니다.

| Secret | 설명 |
|--------|------|
| `OCI_REGISTRY` | OCIR 레지스트리 주소 (e.g. `icn.ocir.io`) |
| `OCI_NAMESPACE` | OCI 네임스페이스 |
| `OCI_USERNAME` | OCIR 로그인 유저 (`namespace/user@tenancy`) |
| `OCI_AUTH_TOKEN` | OCI Auth Token |
| `OCI_HOST` | OCI 인스턴스 IP |
| `OCI_USER` | SSH 접속 유저 (보통 `opc`) |
| `OCI_SSH_KEY` | SSH Private Key |
| `DB_URL` | 프로덕션 DB URL |
| `DB_USERNAME` | 프로덕션 DB 유저 |
| `DB_PASSWORD` | 프로덕션 DB 비밀번호 |

## 프로젝트 구조

```
src/main/java/com/interview/
├── config/          # AppConfig, DataInitializer
├── controller/      # HomeController, RoomController, CommunityController
├── domain/          # JPA Entities + Enums
├── dto/             # Request/Validation DTOs
├── repository/      # Spring Data JPA Repositories
└── service/         # Business Logic
```
