# ---- 빌드 단계: Gradle로 실행 가능한 JAR 생성 ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 의존성 레이어 캐시를 위해 빌드 스크립트/래퍼 먼저 복사
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 소스 복사 후 빌드. 테스트는 CI에서 별도로 돌리므로 이미지 빌드에선 제외한다.
COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- 실행 단계: JRE만 담은 가벼운 런타임 이미지 ----
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 루트가 아닌 사용자로 실행(보안)
RUN useradd -r -u 1001 appuser

# bootJar 결과물만 복사(-plain.jar 제외 패턴)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

USER appuser
EXPOSE 8080

# 컨테이너 메모리에 맞춰 힙을 자동 조정
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
