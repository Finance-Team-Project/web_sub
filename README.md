# Finance News Application

## 프로젝트 소개
이 프로젝트는 사용자 맞춤형 금융 뉴스 서비스를 제공하는 웹 애플리케이션입니다. 사용자들은 금융 뉴스를 쉽게 접하고, 학습하며, 퀴즈를 통해 금융 지식을 테스트할 수 있습니다.

## 주요 기능
1. **뉴스 서비스**
   - 금융 뉴스 제공
   - 뉴스 요약 서비스
   - 키워드 분석 및 워드클라우드
   - 사용자 관심사 기반 뉴스 추천

2. **학습 기능**
   - 금융 용어 사전
   - 용어 학습 시스템
   - 크로스워드 퀴즈
   - 학습 진도 추적

3. **사용자 시스템**
   - 회원가입/로그인
   - 뱃지 시스템
   - 포인트 시스템
   - 관심 카테고리 설정

## 기술 스택
- **Backend**
  - Java 
  - Spring Boot
  - Spring MVC
  - Spring Security
  - JPA/Hibernate

- **Frontend**
  - HTML/CSS
  - JavaScript
  - Thymeleaf

- **Database**
  - SQL (application.properties 설정에 따름)

- **AI/ML**
  - 한국어 자연어 처리 (ko-sent.bin 모델 사용)
  - 뉴스 요약 알고리즘
  - 키워드 추출 시스템

## 프로젝트 구조
```
src/
├── main/
│   ├── java/
│   │   └── Project/Finance_News/
│   │       ├── config/         # 설정 파일
│   │       ├── controller/     # API 및 뷰 컨트롤러
│   │       ├── domain/         # 도메인 모델
│   │       ├── dto/           # 데이터 전송 객체
│   │       ├── repository/    # 데이터 접근 계층
│   │       └── service/       # 비즈니스 로직
│   └── resources/
│       ├── static/           # 정적 파일 (JS, CSS, 이미지)
│       ├── templates/        # Thymeleaf 템플릿
│       └── application.properties  # 애플리케이션 설정
```

## 설치 및 실행 방법
1. 프로젝트 클론
```bash
git clone [repository-url]
```

2. 데이터베이스 설정
- `src/main/resources/application.properties` 파일에서 데이터베이스 설정 수정

3. 프로젝트 빌드 및 실행
```bash
./gradlew build
./gradlew bootRun
```

4. 웹 브라우저에서 접속
```
http://localhost:8080
```

## 주요 API 엔드포인트
- `/api/news`: 뉴스 관련 API
- `/api/quiz`: 퀴즈 관련 API
- `/api/vocabulary`: 용어 학습 관련 API
- `/api/user`: 사용자 관련 API

## 뱃지 시스템
사용자는 다양한 활동을 통해 다음과 같은 뱃지를 획득할 수 있습니다:
- Bronze
- Silver
- Gold
- Platinum

## 개발 가이드라인
1. 코드 컨벤션
   - Java 코드는 Google Java Style Guide를 따릅니다
   - 컨트롤러는 최대한 간단하게 유지하고 비즈니스 로직은 서비스 계층에 작성
   - DTO를 사용하여 계층 간 데이터 전송

2. 테스트
   - 단위 테스트는 `src/test/java` 디렉토리에 작성
   - 테스트 커버리지 유지

## 라이센스
이 프로젝트는 MIT 라이센스를 따릅니다.