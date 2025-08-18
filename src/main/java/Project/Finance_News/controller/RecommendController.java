package Project.Finance_News.controller;

import Project.Finance_News.domain.News;
import Project.Finance_News.repository.NewsRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final NewsRepository newsRepository;

    @GetMapping
    public ResponseEntity<Object> recommend(@RequestParam("user_id") Long userId,
                                            @RequestParam(value = "top_k", required = false, defaultValue = "10") int topK) {
        // 1) Python 호출
        RestTemplate rt = new RestTemplate();
        PythonRecommendResponse py = rt.getForObject("http://localhost:5001/recommend?user_id=" + userId + "&top_k=" + topK,
                PythonRecommendResponse.class);

        if (py == null || py.getCandidates() == null) {
            return ResponseEntity.ok(Map.of("feed", List.of()));
        }

        // 2) news_id로 메타 결합
        List<Long> ids = py.getCandidates().stream().map(Candidate::getNewsId).collect(Collectors.toList());
        List<News> newsList = newsRepository.findAllById(ids);
        Map<Long, News> idToNews = newsList.stream().collect(Collectors.toMap(News::getId, n -> n));

        List<FeedItem> feed = new ArrayList<>();
        for (Candidate c : py.getCandidates()) {
            News n = idToNews.get(c.getNewsId());
            if (n == null) continue;
            feed.add(new FeedItem(n.getId(), n.getTitle(), n.getContent(), n.getImageUrl(), n.getUrl(), n.getPress(), n.getPublishedAt(), c.getScore()));
        }

        // 3) 점수 내림차순 정렬
        feed.sort(Comparator.comparingDouble(FeedItem::getScore).reversed());

        return ResponseEntity.ok(Map.of(
                "request_id", py.getRequestId(),
                "user_id", userId,
                "model_version", py.getModelVersion(),
                "feed", feed
        ));
    }

    @Data
    public static class PythonRecommendResponse {
        private String requestId;
        private Long userId;
        private String modelVersion;
        private List<Candidate> candidates;
    }

    @Data
    public static class Candidate {
        private Long newsId;
        private double score;
    }

    @Data
    @AllArgsConstructor
    public static class FeedItem {
        private Long news_id;
        private String title;
        private String summaryOrContent;
        private String image;
        private String url;
        private String press;
        private java.time.LocalDateTime published_at;
        private double score;

        public double getScore() { return score; }
    }
}


