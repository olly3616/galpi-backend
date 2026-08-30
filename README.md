# 갈피 (Galpi) — Backend

> 좋아하는 책 속 문장을 담아두고, 원하는 시각에 다시 만나는 **구절 저장 앱**의 백엔드 API 서버.

읽다가 마음에 남은 한 문장을 책과 함께 기록하고, 팔로우한 사람들과 나누며, 예약 알림으로 그 문장을 다시 마주하게 하는 서비스입니다.

---

## ✨ 주요 기능

| 영역 | 설명 |
|---|---|
| **인증** | 이메일 회원가입/로그인, JWT 발급, **Refresh 토큰 회전**, 로그아웃 |
| **도서 검색** | 카카오 도서 API 연동으로 책 검색 |
| **책장** | 검색한 책(API) 또는 직접 등록(MANUAL)한 책을 내 책장에 담기/조회/빼기 |
| **대사(구절)** | 책에 문장 기록 — 작성/조회/수정/**소프트 삭제**, 등장인물·메모·공개범위 |
| **예약 알림** | 대사를 특정 시각에 다시 만나도록 예약(매일/매주/한 번), 서버 배치가 푸시 발송 |
| **소셜** | 팔로우/언팔로우, 팔로잉 피드, 좋아요(1인 1회), 사용자 검색·프로필 |
| **푸시** | FCM 디바이스 토큰 등록 및 예약 알림 발송 |

각 대사에는 **출처(책 제목·저자)가 항상 함께 노출**되어 저작권 표기를 지킵니다.

---

## 🛠 기술 스택

| 분류 | 사용 기술 |
|---|---|
| **언어 / 런타임** | Java 17 |
| **프레임워크** | Spring Boot 4.1.1 (Spring MVC, Spring Security, Spring Data JPA) |
| **인증** | JWT (jjwt 0.12.6), 무상태(Stateless) 보안 |
| **DB** | MySQL (운영/개발), H2 (테스트) |
| **API 문서** | springdoc-openapi (Swagger UI) |
| **외부 연동** | 카카오 도서 검색 API, Firebase Cloud Messaging(FCM) |
| **빌드** | Gradle |
| **CI** | GitHub Actions (`./gradlew test`) |

---

## 📁 프로젝트 구조

도메인별 패키지 구성 (`domain/<도메인>/{controller, service, repository, entity, dto}`):

```
com.galpi.galpibackend
├── domain
│   ├── auth          # 회원가입·로그인·토큰
│   ├── book          # 카카오 도서 검색
│   ├── bookshelf     # 책장
│   ├── work          # 책(Work)
│   ├── quote         # 대사
│   ├── schedule      # 예약 알림 + 발송 배치
│   ├── feed          # 팔로잉 피드
│   ├── like          # 좋아요
│   ├── user          # 프로필·팔로우
│   └── devicetoken   # FCM 토큰
└── global            # 보안(JWT)·에러·공통 응답·설정
```

---

## 🚀 실행 방법

### 1. 사전 준비
- JDK 17
- 로컬 MySQL에 `galpi` 데이터베이스 생성

### 2. 환경변수 설정
비밀값은 코드에 두지 않고 환경변수로 주입합니다. 필요한 값은 [`.env.example`](.env.example) 참고:

| 변수 | 설명 |
|---|---|
| `DB_PASSWORD` | MySQL 접속 비밀번호 |
| `JWT_SECRET` | JWT 서명 시크릿 (최소 32바이트) |
| `KAKAO_REST_API_KEY` | 카카오 도서 검색 API 키 |

### 3. 실행
```bash
./gradlew bootRun
```

### 4. API 문서 확인
서버 실행 후 브라우저에서:
```
http://localhost:8080/swagger-ui.html
```

---

## 🧪 테스트

```bash
./gradlew test
```
테스트는 H2 인메모리 DB로 실행되어 별도 DB 설정이 필요 없습니다. Pull Request 시 GitHub Actions가 자동으로 전체 테스트를 수행합니다.

---

## 📖 API 명세

프론트엔드 연동용 API 명세는 Swagger UI(`/swagger-ui.html`)에서 실시간으로 확인·테스트할 수 있습니다.
