/**
 * - REST API 엔드포인트 제공
 * - Swagger 문서화 적용
 * - 주요 엔드포인트:
 *   - POST /api/news: 새 뉴스 등록
 *   - GET /api/news: 모든 뉴스 조회
 *   - GET /api/news/{id}: 특정 뉴스 조회
 * - 각 API에 대한 상세한 설명과 응답 코드 문서화
 *
 * */

package Project.Finance_News.controller;

import Project.Finance_News.domain.News;
import Project.Finance_News.dto.NewsRequestDto;
import Project.Finance_News.dto.NewsResponseDto;
import Project.Finance_News.dto.NewsUploadRequestDto;
import Project.Finance_News.service.news.NewsService;
import Project.Finance_News.service.news.PythonCrawlingRestService;
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "뉴스 API", description = "뉴스 관리를 위한 API")
public class NewsController {

    private final NewsService newsService;
    private final PythonCrawlingRestService pythonCrawlingRestService;

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
        news.setPublisher(newsRequest.getPublisher());
        news.setPublishedAt(newsRequest.getPublishedAt());

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
                .publisher(news.getPublisher())
                .publishedAt(news.getPublishedAt())
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
                    .publisher(news.getPublisher())
                    .publishedAt(news.getPublishedAt())
                    .build();

            return ResponseEntity.ok(responseDto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
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
    public ResponseEntity<String> uploadNewsFromPython(@RequestBody NewsUploadRequestDto request) {
        // 1. News 엔티티 생성 및 저장
        News news = new News();
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setUrl(request.getUrl()); // Set the url from the DTO
        // imageUrl은 News 엔티티에 필드가 있으면 set, 없으면 무시
        Long newsId = newsService.saveNews(news);

        // 2. 용어 저장 (중복 방지)
        if (request.getTerms() != null) {
            for (NewsUploadRequestDto.TermDto termDto : request.getTerms()) {
                newsService.saveOrUpdateTerm(termDto.getTerm(), termDto.getDescription());
                // 필요시 News와 Term의 연관관계(예: NewsKeyword)도 저장
            }
        }
        return ResponseEntity.ok("success");
    }
}
