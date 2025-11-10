package Project.Finance_News.batch.processor;

import Project.Finance_News.domain.News;
import Project.Finance_News.dto.NewsDto;
import Project.Finance_News.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class NewsProcessor implements ItemProcessor<NewsDto, News> {
    private final NewsRepository newsRepository;

    @Override
    public News process(NewsDto dto) {
        try {
            // URL 기반 중복 체크
            if (newsRepository.existsByUrl(dto.getUrl())) {
                log.info("Duplicate news found: {}", dto.getUrl());
                return null;  // 중복된 뉴스는 스킵
            }

            // DTO → Entity 변환
            // DTO에서 언론사 정보 가져오기 (이미 우선순위가 적용된 값)
            String pressValue = dto.getPress();
            
            // 로깅 추가
            log.info("Processing news with press info - Original press: {}, Final press: {}, URL: {}", 
                    dto.getPress(), pressValue, dto.getUrl());

            News news = News.builder()
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .url(dto.getUrl())
                    .imageUrl(dto.getImageUrl())
                    .press(pressValue != null && !pressValue.trim().isEmpty() ? pressValue : "미상")
                    .publishedAt(dto.getPublishedAt())
                    .build();

            log.debug("Processed news: {}", news.getTitle());
            return news;
            
        } catch (Exception e) {
            log.error("Error processing news: {}", e.getMessage());
            return null;  // 에러 발생 시 해당 항목 스킵
        }
    }
}
