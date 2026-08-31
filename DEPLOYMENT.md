# 배포 가이드 (Oracle Cloud VM + Docker Compose + GitHub Actions + Caddy HTTPS)

`main`에 push하면 → CI(테스트) 통과 시 → GitHub Actions가 이미지를 빌드해 GHCR에 올리고 →
서버에 SSH로 접속해 새 이미지로 자동 재기동한다.

```
GitHub(main push) ─► CI 테스트 ─► 이미지 빌드/푸시(GHCR) ─► 서버에서 pull & 재기동
                                                              │
                            서버: docker compose (Caddy ─► Spring Boot ─► MySQL)
```

앱은 **staging 프로파일**로 뜬다(Swagger 켜짐, Flyway가 스키마 생성).
Oracle Cloud **Always Free** VM을 쓰므로 **과금이 없다**(Always Free 자원만 쓰는 한).

---

## A. Oracle Cloud VM 생성 (콘솔)

1. **Compute → Instances → Create instance**
   - 이름: `galpi-server`
   - 이미지: **Canonical Ubuntu 22.04**
   - Shape: **VM.Standard.E2.1.Micro** (AMD, **Always Free** 표시, 1 OCPU / 1GB)
     - (참고: Ampere ARM A1은 24GB까지 무료지만 ARM용 이미지가 필요 → 지금은 AMD micro 권장. ARM 원하면 알려주세요, 멀티아치 빌드 추가해드립니다.)
   - **SSH 키**: "Generate a key pair for me" → **개인 키(.key) 다운로드**해서 안전히 보관(재발급 불가).
2. **네트워킹**: 퍼블릭 IP 자동 할당 확인. 생성 후 **퍼블릭 IPv4 주소** 기록.
3. **방화벽 1 — VCN Security List (인바운드 규칙 추가)**
   Networking → 해당 VCN → Security Lists → Default → Add Ingress Rules:
   | 소스 | 포트 | 용도 |
   |---|---|---|
   | 0.0.0.0/0 | 22 | SSH(기본 존재) |
   | 0.0.0.0/0 | 80 | HTTP(인증서 발급·리다이렉트) |
   | 0.0.0.0/0 | 443 | HTTPS |
   | 0.0.0.0/0 | 8080 | 앱(도메인 붙이기 전 직접 접속용) |
   - ⚠ **3306(MySQL)은 열지 말 것.**

---

## B. 서버 초기 세팅 (SSH 접속 후 1회)

```bash
# 로컬에서 접속 (다운로드한 키 위치에서)
chmod 400 galpi-server.key
ssh -i galpi-server.key ubuntu@<서버_퍼블릭_IP>
```

서버 안에서:
```bash
# 1) ⚠ Oracle 필수: 방화벽 2 — 인스턴스 내부 iptables 열기
#    (Oracle Ubuntu 이미지는 Security List를 열어도 내부 iptables가 막아 접속이 안 된다)
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo netfilter-persistent save

# 2) Docker 설치
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
newgrp docker

# 3) 1GB 메모리 보완용 스왑 2GB (micro 인스턴스 필수)
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 4) 저장소 클론
git clone https://github.com/olly3616/galpi-backend.git
cd galpi-backend

# 5) 비밀값 파일 작성 (.env — git에 올라가지 않음)
nano .env
```

`.env` 내용 (값은 본인 것으로):
```dotenv
DB_PASSWORD=강한_DB_비밀번호
JWT_SECRET=최소_32바이트_이상의_긴_랜덤_문자열
KAKAO_REST_API_KEY=카카오_REST_키
# 선택(이미지 업로드 쓰면): AWS_REGION / AWS_S3_BUCKET / AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY
# 선택(FCM 쓰면): FCM_ENABLED=true / FCM_CREDENTIALS_PATH=/app/firebase-service-account.json
# HTTPS 켤 때만(F단계): DOMAIN=galpi.duckdns.org / COMPOSE_PROFILES=tls
```

---

## C. GitHub 설정 (자동 배포용)

### C-1. 배포용 시크릿 등록
GitHub 저장소 → **Settings → Secrets and variables → Actions → New repository secret** 로 3개 등록:

| 이름 | 값 |
|---|---|
| `SSH_HOST` | 서버 퍼블릭 IP |
| `SSH_USER` | `ubuntu` |
| `SSH_KEY` | 다운로드한 개인 키(.key) **내용 전체** |

### C-2. GHCR 이미지 공개로 전환
첫 배포로 이미지가 한 번 올라간 뒤, GitHub 프로필 → **Packages → galpi-backend → Package settings → Change visibility → Public**.
(서버가 로그인 없이 `docker compose pull` 하도록. 이미지에는 비밀값이 없다 — 비밀값은 서버 `.env`에서 런타임에만 주입.)

---

## D. 첫 배포

```bash
# 서버의 ~/galpi-backend 에서 (처음엔 직접 빌드 — 이미지 공개 전이라)
docker compose up -d --build
docker compose logs -f app       # 기동 로그 확인 (Ctrl+C로 빠져나옴)
```
이후로는 `main`에 push → CI 통과 → Actions가 자동 재배포.

---

## E. 동작 확인 (HTTP)

```bash
curl http://<서버_퍼블릭_IP>:8080/swagger-ui.html   # 200이면 성공
```
- Swagger: `http://<서버_IP>:8080/swagger-ui.html`
- 시연용으로는 여기까지로 충분(프론트 API base URL = `http://<서버_IP>:8080`).

---

## F. HTTPS 붙이기 (무료: Caddy + DuckDNS)

앱스토어 배포 앱은 공개 https가 필수다. 도메인·인증서 모두 무료로 만들 수 있다.

1. **무료 도메인 (DuckDNS)**
   - https://www.duckdns.org 접속 → 소셜 로그인 → 서브도메인 생성(예: `galpi`)
   - 그 도메인의 IP를 **서버 퍼블릭 IP**로 지정 → `galpi.duckdns.org` 완성
   - (커스텀 도메인을 샀다면 A레코드를 서버 IP로 연결하면 동일)
2. **서버 `.env`에 두 줄 추가**
   ```dotenv
   DOMAIN=galpi.duckdns.org
   COMPOSE_PROFILES=tls
   ```
3. **재기동** — Caddy가 함께 뜨며 Let's Encrypt 인증서를 자동 발급
   ```bash
   docker compose up -d
   docker compose logs -f caddy      # "certificate obtained" 뜨면 성공
   ```
4. **확인**: `https://galpi.duckdns.org/swagger-ui.html`
5. 프론트 EAS 환경변수: `EXPO_PUBLIC_API_URL=https://galpi.duckdns.org`

> 인증서 발급은 80/443 포트가 외부에서 닿아야 한다(A·B단계의 Security List + iptables 확인).

---

## 트러블슈팅

| 증상 | 확인 |
|---|---|
| 폰/브라우저에서 접속 안 됨 | **Oracle 2중 방화벽**: Security List(콘솔) + iptables(B-1) 둘 다 열었는지 |
| 앱이 바로 죽음 | `docker compose logs app` — 대개 `.env`의 `JWT_SECRET`(32바이트↑)/`DB_PASSWORD` 누락 |
| 메모리 부족(OOM) 재시작 반복 | B-3 스왑 추가했는지 확인 |
| DB 연결 실패 | `docker compose ps`로 mysql healthy 확인 |
| Caddy 인증서 실패 | DOMAIN이 서버 IP를 정확히 가리키는지, 80/443 열렸는지 |
| GHCR pull 권한 오류 | C-2 이미지 공개 전환 확인 |
