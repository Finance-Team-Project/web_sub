package Project.Finance_News.service.news;

import Project.Finance_News.domain.News;
import Project.Finance_News.domain.NewsKeyword;
import Project.Finance_News.domain.User;
import Project.Finance_News.dto.NewsCacheRequestDto;
import Project.Finance_News.dto.RecommendRequestDto;
import Project.Finance_News.dto.RecommendResponseDto;
import Project.Finance_News.dto.RecommendationDto;
import Project.Finance_News.repository.NewsKeywordRepository;
import Project.Finance_News.repository.UserNewsLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FastApiService {
    private final RestTemplate restTemplate;
    private final UserNewsLogRepository userNewsLogRepository;
    private final NewsKeywordRepository newsKeywordRepository;
    
    @Value("${fastapi.server.url:http://localhost:8000}")
    private String fastApiBaseUrl;
    
    @Value("${fastapi.retry.max-attempts:2}")
    private int maxRetryAttempts;

    public void updateNewsCache(News news) {
        String url = fastApiBaseUrl + "/cache/update";
        
        try {
            List<NewsKeyword> newsKeywords = newsKeywordRepository.findByNews(news);
            NewsCacheRequestDto cacheRequest = NewsCacheRequestDto.from(news, newsKeywords);
            
            log.info("FastAPI 캐시 업데이트 요청 - 뉴스 ID: {}, 키워드 수: {}, URL: {}", 
                news.getId(), 
                newsKeywords.size(),
                url);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.postForEntity(url, cacheRequest, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("FastAPI 캐시 업데이트 성공 - 뉴스 ID: {}, 응답: {}", 
                    news.getId(), 
                    response.getBody());
            } else {
                log.warn("FastAPI 캐시 업데이트 실패 - 뉴스 ID: {}, 상태 코드: {}", 
                    news.getId(), 
                    response.getStatusCode());
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // FastAPI 서버가 실행되지 않은 경우 - 로그만 출력하고 계속 진행
            log.warn("FastAPI 서버에 연결할 수 없습니다 (서버가 실행 중이지 않을 수 있음). 뉴스 ID: {}, URL: {}", 
                news.getId(), url);
        } catch (Exception e) {
            // 기타 예외는 로그만 출력하고 계속 진행 (애플리케이션 중단 방지)
            log.error("FastAPI 캐시 업데이트 실패 - 뉴스 ID: {}, 에러: {}", 
                news.getId(), 
                e.getMessage());
        }
    }

    public RecommendResponseDto getRecommendations(User user) {
        String url = fastApiBaseUrl + "/recommend";
        
        List<Object[]> clickCounts = userNewsLogRepository.findNewsClickCountByUser(user);
        List<Long> clickedNewsIds = clickCounts.stream()
            .map(array -> (Long) array[0])
            .collect(Collectors.toList());
        
        RecommendRequestDto request = new RecommendRequestDto();
        request.setUserId(user.getId());
        request.setClickedNewsIds(clickedNewsIds);
        
        try {
            RecommendResponseDto response = restTemplate.postForObject(
                url, request, RecommendResponseDto.class);
            
            if (response != null && response.getRecommendations() != null) {
                for (RecommendationDto rec : response.getRecommendations()) {
                    log.info("추천 뉴스 - ID: {}, 제목: {}, 매칭 키워드: {}", 
                        rec.getNewsId(), rec.getTitle(), 
                        String.join(", ", rec.getMatchedKeywords()));
                }
            }
            
            return response;
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // FastAPI 서버가 실행되지 않은 경우 - 빈 응답 반환
            log.warn("FastAPI 서버에 연결할 수 없습니다 (서버가 실행 중이지 않을 수 있음). userId: {}, URL: {}", 
                user.getId(), url);
            return new RecommendResponseDto(); // 빈 응답 반환
        } catch (Exception e) {
            // 기타 예외는 로그만 출력하고 빈 응답 반환
            log.error("추천 요청 실패 (userId={}): {}", user.getId(), e.getMessage());
            return new RecommendResponseDto(); // 빈 응답 반환
        }
    }
}
