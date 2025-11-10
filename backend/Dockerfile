FROM gradle:8.10.2-jdk17-alpine AS builder

WORKDIR /workspace

# Gradle 빌드 캐시를 활용하기 위해 먼저 빌드 스크립트만 복사
COPY build.gradle settings.gradle* ./
RUN mkdir -p src

# 의존성만 사전 다운로드 (실패해도 다음 단계 진행)
RUN gradle --no-daemon build -x test || true

# 실제 소스 복사
COPY . .

# gradle wrapper가 없을 경우 대비
RUN gradle wrapper --no-daemon || true

# gradlew 실행 권한 보장
RUN chmod +x gradlew

# Spring Boot JAR 빌드
RUN ./gradlew clean bootJar --no-daemon -x test


FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# 타임존 설정 및 헬스체크 도구 설치
RUN apk add --no-cache tzdata wget && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

# 빌드 결과물 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

# 헬스체크 (Actuator 사용 시 엔드포인트 조정 권장)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=5 \
  CMD wget -qO- http://localhost:8080/ || exit 1

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
    