package Project.Finance_News.batch.reader;

import Project.Finance_News.dto.NewsApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class RestApiReader implements ItemReader<NewsApiResponse> {
    private final RestTemplate restTemplate;
    private boolean dataFetched = false;
    
    @Value("${python.api.url}")
    private String apiUrl;
    
    @Value("${python.api.endpoints.crawl}")
    private String crawlEndpoint;

    @Override
    public NewsApiResponse read() {
        if (dataFetched) {
            return null;  // 한 번만 실행
        }
        
        try {
            dataFetched = true;
            String url = apiUrl + crawlEndpoint;
            log.info("Fetching news from Python API: {}", url);
            return restTemplate.postForObject(url, null, NewsApiResponse.class);
        } catch (Exception e) {
            log.error("Error fetching news from Python API: {}", e.getMessage());
            throw e;
        }
    }
}
