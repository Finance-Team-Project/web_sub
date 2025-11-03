/**
 * - REST API 엔드포인트 제공
 * - Swagger 문서화 적용
 * - 주요 엔드포인트:
 *   - POST /api/news: 새 뉴스 등록
 *   - GET /api/news: 모든 뉴스 조회
 *   - GET  /api/news/{id}: 특정 뉴스 조회
 * - 각 API에 대한 상세한 설명과 응답 코드 문서화
 *
 * */

package Project.Finance_News.controller;

import Project.Finance_News.domain.News;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.session.SessionConst;
import Project.Finance_News.dto.NewsRequestDto;
import Project.Finance_News.dto.NewsResponseDto;
import Project.Finance_News.dto.NewsUploadRequestDto;
import Project.Finance_News.service.news.NewsService;
import Project.Finance_News.service.news.PythonCrawlingRestService;
import Project.Finance_News.service.news.FastApiService;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;
import Project.Finance_News.dto.TermDto;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "뉴스 API", description = "뉴스 관리를 위한 API")
public class NewsController {

    private final NewsService newsService;
    private final PythonCrawlingRestService pythonCrawlingRestService;
    private final FastApiService fastApiService;

    @Operation(summary = "뉴스 등록", description = "새로운 뉴스 기사를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "뉴스 등록 성공",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/api/news")
    public ResponseEntity<Long> createNews(@RequestBody NewsRequestDto newsRequest) {
        News news = new News();
        news.setTitle(newsRequest.getTitle());
        news.setContent(newsRequest.getContent());
        news.setPress(newsRequest.getPress());
        
        // 날짜 처리: String이 있으면 String에서 변환, 없으면 LocalDateTime 사용
        LocalDateTime publishedAt = newsRequest.getPublishedAtFromString();
        if (publishedAt == null) {
            publishedAt = newsRequest.getPublishedAt();
        }
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now(); // 기본값으로 현재 시간 사용
        }
        news.setPublishedAt(publishedAt);

        Long savedId = newsService.saveNews(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedId);
    }

    @Operation(summary = "뉴스 전체 조회", description = "모든 뉴스 기사를 조회합니다.")
    @GetMapping("/api/news")
    public ResponseEntity<Page<NewsResponseDto>> getNewsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) { // Changed from 10 to 8
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<News> newsPage = newsService.getNewsPaged(pageable);
        Page<NewsResponseDto> dtoPage = newsPage.map(news -> NewsResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .press(news.getPress())
                .publishedAt(news.getPublishedAt())
                .imageUrl(news.getImageUrl())
                .url(news.getUrl())
                .build());
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "뉴스 상세 조회", description = "특정 ID의 뉴스 기사를 조회하며, 본문 내 금융 용어는 강조되어 반환됩니다.")
    @GetMapping("/api/news/{id}")
    public ResponseEntity<NewsResponseDto> getNewsById(@PathVariable Long id) {
        try {
            News news = newsService.getNewsWithHighlight(id); // 용어 강조된 뉴스 가져오기

            NewsResponseDto responseDto = NewsResponseDto.builder()
                    .id(news.getId())
                    .title(news.getTitle())
                    .content(news.getContent()) // 강조된 콘텐츠 포함
                    .press(news.getPress())
                    .publishedAt(news.getPublishedAt())
                    .imageUrl(news.getImageUrl())
                    .url(news.getUrl())
                    .build();

            return ResponseEntity.ok(responseDto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 1. 뉴스별 키워드 조회
    @GetMapping("/api/news/{id}/keywords")
    public ResponseEntity<List<String>> getKeywords(@PathVariable Long id) {
        List<String> keywords = newsService.getTopKeywords(id);
        return ResponseEntity.ok(keywords);
    }

    // 2. 키워드로 뉴스 목록 조회 (페이징)
    @GetMapping(value = "/api/news", params = "keyword")
    public ResponseEntity<Page<NewsResponseDto>> getNewsByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<NewsResponseDto> newsPage = newsService.findByKeyword(keyword, pageable);
        return ResponseEntity.ok(newsPage);
    }

    @PostMapping("/crawl-python-rest")
    public ResponseEntity<String> crawlNewsWithPythonRest() {
        try {
            String result = pythonCrawlingRestService.crawlNewsViaRest();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("크롤링 실패: " + e.getMessage());
        }
    }

    @PostMapping("/news/upload")
    public ResponseEntity<Object> uploadNewsFromPython(@RequestBody NewsUploadRequestDto request) {
        // 1. 뉴스 저장
        News news = new News();
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setUrl(request.getUrl());
        news.setImageUrl(request.getEffectiveImageUrl());
        news.setPress(request.getPress());
        
        // 날짜 처리: String이 있으면 String에서 변환, 없으면 LocalDateTime 사용
        LocalDateTime publishedAt = request.getPublishedAtFromString();
        if (publishedAt == null) {
            publishedAt = request.getPublishedAt();
        }
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now(); // 기본값으로 현재 시간 사용
        }
        news.setPublishedAt(publishedAt);

        Long newsId = newsService.saveNewsWithKeywords(news, request.getKeywords());

        // 2. FastAPI 캐시 업데이트 (비동기로 처리)
        if (newsId != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    fastApiService.updateNewsCache(news);
                    log.info("FastAPI 캐시 업데이트 성공 - 뉴스 ID: {}", newsId);
                } catch (Exception e) {
                    log.error("FastAPI 캐시 업데이트 실패 - 뉴스 ID: {}, 에러: {}", newsId, e.getMessage());
                }
            });
        }

        // 3. 용어 저장 (여러 설명 지원)
        if (request.getTerms() != null) {
            // (기존) for (NewsUploadRequestDto.TermDto termDto : request.getTerms()) { ... }
            // (변경) 아래처럼 여러 설명을 처리하는 서비스 메서드로 전달
            List<TermDto> termDtoList = request.getTerms().stream().map(t -> {
                TermDto dto = new TermDto();
                dto.setTerm(t.getTerm());
                dto.setDesc1(t.getDesc1());
                dto.setDesc2(t.getDesc2());
                dto.setDesc3(t.getDesc3()); // 이 부분은 추가된 부분
                return dto;
            }).collect(Collectors.toList());
            newsService.saveTermsAndGlossaries(termDtoList);
        }

        if (newsId == null) {
            // 중복 저장 시 기존 ID 반환할 수 있도록 URL로 조회
            return ResponseEntity.status(HttpStatus.OK).body(java.util.Map.of(
                    "status", "duplicate",
                    "news_id", null
            ));
        }

        return ResponseEntity.status(HttpStatus.OK).body(java.util.Map.of(
                "news_id", newsId,
                "status", "saved"
        ));
    }

    /**
     * 뉴스 클릭 로그 저장
     */
    @PostMapping("/api/news/{newsId}/click")
    public ResponseEntity<Void> logNewsClick(
            @PathVariable Long newsId,
            @SessionAttribute(name = SessionConst.LOGIN_USER) User loginUser) {
        News news = newsService.findById(newsId).orElse(null);
        if (news == null) {
            return ResponseEntity.notFound().build();
        }
        newsService.logNewsClick(loginUser, news);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "나의 관심 뉴스(클릭수 기준)", description = "사용자별로 많이 클릭한 뉴스 리스트를 반환합니다.")
    @GetMapping("/api/news/interest")
    public ResponseEntity<List<NewsResponseDto>> getUserInterestNewsByClickCount(
            @RequestParam(defaultValue = "5") int limit,
            HttpSession session) {
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<News> newsList = newsService.getUserInterestNewsByClickCount(user, limit);
        List<NewsResponseDto> dtoList = newsList.stream().map(news -> NewsResponseDto.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .press(news.getPress())
                .publishedAt(news.getPublishedAt())
                .imageUrl(news.getImageUrl())
                .url(news.getUrl())
                .build()).toList();
        return ResponseEntity.ok(dtoList);
    }
}
