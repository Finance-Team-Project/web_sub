package Project.Finance_News.service.news;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;

@Service
public class PythonCrawlingRestService {
    private final String pythonApiUrl;
    private final String crawlEndpoint;
    private final RestTemplate restTemplate;

    public PythonCrawlingRestService(
            @Value("${python.api.url}") String pythonApiUrl,
            @Value("${python.api.endpoints.crawl}") String crawlEndpoint,
            RestTemplate restTemplate) {
        this.pythonApiUrl = pythonApiUrl;
        this.crawlEndpoint = crawlEndpoint;
        this.restTemplate = restTemplate;
    }

    public String crawlNewsViaRest() {
        String url = pythonApiUrl + crawlEndpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody(); // JSON 결과
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Python API 서버가 실행되지 않은 경우
            System.err.println("[크롤링] Python API 서버에 연결할 수 없습니다 (서버가 실행 중이지 않을 수 있음). URL: " + url);
            return "{\"error\": \"Python API 서버에 연결할 수 없습니다\"}";
        } catch (Exception e) {
            // 기타 예외
            System.err.println("[크롤링] 크롤링 실패: " + e.getMessage());
            return "{\"error\": \"크롤링 실패: " + e.getMessage() + "\"}";
        }
    }

    @Scheduled(fixedRate = 120000) // 2분(120000ms)마다 실행
    public void scheduledCrawling() {
        try {
            String result = crawlNewsViaRest();
            System.out.println("[스케줄러] 크롤링 결과: " + result);
            // TODO: 결과를 DB에 저장하거나 추가 후처리 가능
        } catch (Exception e) {
            System.err.println("[스케줄러] 크롤링 실패: " + e.getMessage());
        }
    }
} 