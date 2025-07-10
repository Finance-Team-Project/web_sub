package Project.Finance_News.service.news;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class PythonCrawlingRestService {
    public String crawlNewsViaRest() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5000/crawl";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>("{}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody(); // JSON 결과
    }

    @Scheduled(cron = "0 0 * * * *") // 매시 정각마다 실행
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