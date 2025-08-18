package Project.Finance_News.controller;

import Project.Finance_News.domain.News;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.UserNewsLog;
import Project.Finance_News.repository.NewsRepository;
import Project.Finance_News.repository.UserRepository;
import Project.Finance_News.repository.UserNewsLogRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventsController {

    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final UserNewsLogRepository userNewsLogRepository;

    @PostMapping("/batch")
    public ResponseEntity<Object> ingestEvents(@RequestBody EventsBatchRequest request) {
        if (request == null || request.getEvents() == null || request.getEvents().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "empty"));
        }

        for (EventPayload e : request.getEvents()) {
            if (!"view".equalsIgnoreCase(e.getEventType())) continue;
            User user = userRepository.findById(e.getUserId()).orElse(null);
            News news = newsRepository.findById(e.getNewsId()).orElse(null);
            if (user == null || news == null) continue;

            UserNewsLog log = new UserNewsLog();
            log.setUser(user);
            log.setNews(news);
            log.setViewedAt(e.getTimestamp() != null ? e.getTimestamp() : LocalDateTime.now());
            userNewsLogRepository.save(log);
        }

        // Fire-and-forget로 Python에 전달
        try {
            RestTemplate rt = new RestTemplate();
            rt.postForEntity("http://localhost:5001/events/batch", request, String.class);
        } catch (Exception ignored) { }

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @Data
    public static class EventsBatchRequest {
        private List<EventPayload> events;
    }

    @Data
    public static class EventPayload {
        private String eventId;
        private Long userId;
        private Long newsId;
        private String eventType;
        private LocalDateTime timestamp;
        private Long dwellTimeMs;
    }
}



