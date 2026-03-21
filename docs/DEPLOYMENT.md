# 배포 구조 및 흐름

## 개요

| 항목 | 내용 |
|------|------|
| 서버 | AWS EC2 (ap-northeast-2, Ubuntu) |
| 도메인 | withstudy.kro.kr |
| 프론트엔드 레포 | forSideApps/WithStudy-FE |
| 백엔드 레포 | forSideApps/WithStudy-BE |
| 컨테이너 레지스트리 | GitHub Container Registry (ghcr.io) |
| DB | Oracle Autonomous Database (OCI, ap-chuncheon-1) |
| 파일 스토리지 | OCI Object Storage |

---

## 전체 아키텍처

```
[ 사용자 브라우저 ]
        │  HTTPS (443)
        ▼
[ EC2 — 시스템 Nginx ]  ← SSL 종료 (Let's Encrypt)
        │  HTTP (8880)
        ▼
[ Docker: withstudy-nginx ]  ← 정적 파일 서빙 + /api/ 프록시
        │  HTTP (21002, Docker 내부)
        ▼
[ Docker: withstudy-app ]  ← Spring Boot
        │
        ▼
[ Oracle ADB (OCI) ]  ← 데이터베이스 (mTLS 지갑 인증)
```

---

## CI/CD 흐름

### 프론트엔드 (WithStudy-FE)

```
main 브랜치 push
        │
        ▼
[ GitHub Actions ]
  1. npm ci && npm run build         → React 빌드 (dist/)
  2. dist/ → nginx/dist/ 복사
  3. docker build nginx/             → Nginx 이미지 (정적 파일 포함)
  4. docker push ghcr.io/...withstudy-nginx:latest
        │
        ▼  SSH (EC2_KEY)
[ EC2 ]
  5. docker compose pull nginx
  6. docker compose up -d --no-deps nginx
```

### 백엔드 (WithStudy-BE)

```
main 브랜치 push
        │
        ▼
[ GitHub Actions ]
  1. docker build ./backend          → Spring Boot 이미지 (Dockerfile)
  2. docker push ghcr.io/...withstudy-backend:latest
        │
        ▼  SSH (EC2_KEY)
[ EC2 ]
  3. WALLET_BASE64 복원              → backend/wallet/
  4. OCI_BASE64 복원                 → oci/config + oci_api_key.pem
  5. .env 생성                       → DB_USERNAME, DB_PASSWORD
  6. docker compose pull app
  7. docker compose up -d --no-deps app
```

---

## EC2 디렉토리 구조

```
/home/ubuntu/withstudy/           ← WithStudy-FE 레포 클론
├── docker-compose.prod.yml
├── .env                          ← 배포 시 생성 (DB 크리덴셜)
├── backend/
│   └── wallet/                   ← Oracle 지갑 (WALLET_BASE64 복원)
│       ├── cwallet.sso
│       ├── tnsnames.ora
│       ├── sqlnet.ora            ← /wallet 경로로 고정 (배포 시 덮어쓰기)
│       └── ...
└── oci/                          ← OCI 크리덴셜 (OCI_BASE64 복원)
    ├── config
    └── oci_api_key.pem
```

---

## Docker Compose (docker-compose.prod.yml)

```yaml
services:
  app:                            # Spring Boot
    image: ghcr.io/forsideapps/withstudy-backend:latest
    environment:
      - DB_USERNAME, DB_PASSWORD, TNS_ADMIN=/wallet
    volumes:
      - ./backend/wallet:/wallet:ro
      - ./oci/config:/root/.oci/config:ro
      - ./oci/oci_api_key.pem:/root/.oci/oci_api_key.pem:ro
    mem_limit: 400m

  nginx:                          # React 정적 파일 + API 프록시
    image: ghcr.io/forsideapps/withstudy-nginx:latest
    ports:
      - "8880:80"                 # 시스템 Nginx가 SSL 처리 후 여기로 프록시
    mem_limit: 64m
```

---

## 시스템 Nginx (EC2 호스트)

`/etc/nginx/sites-available/withstudy.kro.kr`

```nginx
server {
    server_name withstudy.kro.kr;

    location / {
        proxy_pass http://localhost:8880;  # Docker Nginx로 전달
    }

    listen 443 ssl;  # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/withstudy.kro.kr/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/withstudy.kro.kr/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
}

server {
    if ($host = withstudy.kro.kr) {
        return 301 https://$host$request_uri;
    }
    listen 80;
    server_name withstudy.kro.kr;
    return 404;
}
```

---

## GitHub Secrets

### WithStudy-FE (3개)

| Secret | 설명 |
|--------|------|
| `EC2_HOST` | EC2 퍼블릭 도메인 |
| `EC2_USER` | EC2 SSH 유저 (`ubuntu`) |
| `EC2_KEY` | EC2 SSH 개인키 (PEM) |

### WithStudy-BE (7개)

| Secret | 설명 |
|--------|------|
| `EC2_HOST` | EC2 퍼블릭 도메인 |
| `EC2_USER` | EC2 SSH 유저 (`ubuntu`) |
| `EC2_KEY` | EC2 SSH 개인키 (PEM) |
| `DB_USERNAME` | Oracle DB 유저명 |
| `DB_PASSWORD` | Oracle DB 비밀번호 |
| `WALLET_BASE64` | Oracle 지갑 tar.gz → base64 인코딩 값 |
| `OCI_BASE64` | `~/.oci/` 디렉토리 tar.gz → base64 인코딩 값 |

### Secret 생성 방법

```bash
# WALLET_BASE64 — backend/wallet 디렉토리 기준
cd backend && tar czf - wallet | base64

# OCI_BASE64 — 로컬 ~/.oci 디렉토리 기준
tar czf - -C ~/.oci . | base64
```

---

## Oracle DB 연결 방식

Spring Boot → JDBC (`oracle.jdbc.OracleDriver`) → TNS (`clouddb_high`) → mTLS (지갑)

- `TNS_ADMIN=/wallet` 환경변수로 지갑 경로 지정
- `sqlnet.ora`의 `WALLET_LOCATION` 경로를 `/wallet`으로 고정 (배포 시 자동 덮어쓰기)
- 지갑 파일은 git에 포함하지 않고 `WALLET_BASE64` 시크릿으로 관리

---

## SSL 인증서

- 발급: `certbot` + Let's Encrypt (nginx 플러그인)
- 경로: `/etc/letsencrypt/live/withstudy.kro.kr/`
- 자동 갱신: certbot systemd timer (설치 시 자동 설정)
