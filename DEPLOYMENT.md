# 배포 가이드 (AWS EC2 + Docker Compose + GitHub Actions)

`main`에 push하면 → CI(테스트) 통과 시 → GitHub Actions가 이미지를 빌드해 GHCR에 올리고 →
EC2에 SSH로 접속해 새 이미지로 자동 재기동한다.

```
GitHub(main push) ─► CI 테스트 ─► 이미지 빌드/푸시(GHCR) ─► EC2에서 pull & 재기동
                                                              │
                                   EC2: docker compose (Spring Boot + MySQL)
```

앱은 **staging 프로파일**로 뜬다(Swagger 켜짐, Flyway가 스키마 생성). 스키마 변경은 `src/main/resources/db/migration/V2__*.sql` 처럼 마이그레이션을 추가한다.

---

## A. EC2 인스턴스 생성 (AWS 콘솔)

1. **EC2 → 인스턴스 시작**
   - AMI: **Ubuntu Server 22.04 LTS**
   - 인스턴스 타입: **t3.small (권장, 2GB)**. 프리티어 t2.micro(1GB)도 가능하나 메모리가 빠듯 → 아래 "스왑" 참고.
   - 키페어: **새로 생성**해서 `.pem` 파일 다운로드(잃어버리면 접속 불가).
2. **보안 그룹(방화벽)** 인바운드 규칙:
   | 포트 | 소스 | 용도 |
   |---|---|---|
   | 22 (SSH) | 내 IP | 서버 접속 |
   | 8080 (TCP) | 0.0.0.0/0 | 앱 API (시연용 공개) |
   - ⚠ **3306(MySQL)은 절대 열지 말 것.** DB는 컨테이너 내부에서만 접근한다.
3. 인스턴스의 **퍼블릭 IPv4 주소**를 적어둔다 (예: `3.34.xx.xx`).

---

## B. 서버 초기 세팅 (SSH 접속 후 1회)

```bash
# 로컬에서 접속 (.pem 위치에서)
chmod 400 galpi-key.pem
ssh -i galpi-key.pem ubuntu@<EC2_퍼블릭_IP>
```

서버 안에서:
```bash
# 1) Docker 설치
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu          # sudo 없이 docker 쓰기
newgrp docker                            # 그룹 즉시 적용(또는 재접속)

# 2) 저장소 클론
git clone https://github.com/olly3616/galpi-backend.git
cd galpi-backend

# 3) 비밀값 파일 작성 (.env — git에 올라가지 않음)
nano .env
```

`.env` 내용 (값은 본인 것으로):
```dotenv
DB_PASSWORD=강한_DB_비밀번호
JWT_SECRET=최소_32바이트_이상의_긴_랜덤_문자열
KAKAO_REST_API_KEY=카카오_REST_키
# 선택(이미지 업로드 쓰면):
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=버킷명
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
# 선택(FCM 쓰면): FCM_ENABLED=true / FCM_CREDENTIALS_PATH=/app/firebase-service-account.json
```

> **t2.micro(1GB) 사용 시 스왑 추가**(메모리 부족 방지):
> ```bash
> sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
> sudo mkswap /swapfile && sudo swapon /swapfile
> echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
> ```

---

## C. GitHub 설정 (자동 배포용)

### C-1. 배포용 시크릿 등록
GitHub 저장소 → **Settings → Secrets and variables → Actions → New repository secret** 로 3개 등록:

| 이름 | 값 |
|---|---|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_USER` | `ubuntu` |
| `EC2_SSH_KEY` | `.pem` 파일 **내용 전체** (`-----BEGIN ...` 부터 끝까지) |

### C-2. GHCR 이미지 공개로 전환
첫 배포로 이미지가 한 번 올라간 뒤, GitHub 프로필 → **Packages → galpi-backend → Package settings → Change visibility → Public** 로 바꾼다.
(EC2가 로그인 없이 `docker compose pull` 할 수 있게. 이미지에는 비밀값이 들어있지 않다 — 비밀값은 서버 `.env`에서 런타임에만 주입된다.)

---

## D. 첫 배포

### 방법 1: 서버에서 수동으로 한 번 (권장 — 동작 확인)
```bash
# 서버의 ~/galpi-backend 에서
docker compose up -d --build     # 처음엔 직접 빌드(이미지 공개 전이라)
docker compose logs -f app       # 기동 로그 확인 (Ctrl+C로 빠져나옴)
```

### 방법 2: 이후로는 자동
`main`에 push → CI 통과 → Actions가 알아서 EC2에 재배포.

---

## E. 동작 확인

```bash
# 서버 또는 로컬에서
curl http://<EC2_퍼블릭_IP>:8080/swagger-ui.html   # 200이면 성공
```
- Swagger: `http://<EC2_IP>:8080/swagger-ui.html`
- 프론트(Expo) API base URL을 `http://<EC2_IP>:8080` 으로 설정 → 참석자 폰에서 접속.

---

## F. (선택) HTTPS 붙이기 — 나중에

지금은 HTTP(공개 IP)로 충분. 제대로 된 도메인+HTTPS가 필요해지면:
- 무료 도메인(DuckDNS 등) 또는 보유 도메인 연결
- **Caddy** 리버스 프록시 컨테이너를 compose에 추가하면 자동으로 Let's Encrypt HTTPS 발급
- 필요 시 그때 도와드림.

---

## 트러블슈팅

| 증상 | 확인 |
|---|---|
| 앱이 바로 죽음 | `docker compose logs app` — 대개 `.env`의 `JWT_SECRET` 누락(32바이트 미만)/`DB_PASSWORD` 누락 |
| DB 연결 실패 | mysql 컨테이너 healthy 여부: `docker compose ps` |
| 메모리 부족(OOM)으로 재시작 반복 | t2.micro면 위 스왑 추가, 또는 t3.small로 상향 |
| 폰에서 접속 안 됨 | 보안그룹 8080 인바운드 열렸는지, `http://`(https 아님)로 접속하는지 |
| GHCR pull 권한 오류 | C-2 이미지 공개 전환 확인 |
