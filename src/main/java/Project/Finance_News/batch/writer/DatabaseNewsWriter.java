package Project.Finance_News.batch.writer;

import Project.Finance_News.domain.News;
import Project.Finance_News.domain.NewsKeyword;
import Project.Finance_News.repository.NewsRepository;
import Project.Finance_News.repository.NewsKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class DatabaseNewsWriter implements ItemWriter<News> {
    private final NewsRepository newsRepository;
    private final NewsKeywordRepository newsKeywordRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends News> chunk) {
        try {
            for (News news : chunk.getItems()) {
                // URL 기반 중복 체크
                if (newsRepository.existsByUrl(news.getUrl())) {
                    log.info("Skip duplicate news: {}", news.getUrl());
                    continue;
                }

                // 1. 뉴스 저장
                News savedNews = newsRepository.save(news);
                log.debug("Saved news: {}", savedNews.getTitle());

                // 2. 키워드 저장
                if (news.getKeywords() != null && !news.getKeywords().isEmpty()) {
                    for (String keyword : news.getKeywords()) {
                        NewsKeyword newsKeyword = NewsKeyword.builder()
                                .news(savedNews)
                                .keyword(keyword)
                                .build();
                        savedNews.addNewsKeyword(newsKeyword);
                    }
                    log.debug("Added {} keywords for news: {}", 
                            news.getKeywords().size(), savedNews.getTitle());
                }

                // 3. 변경사항 저장
                newsRepository.save(savedNews);
            }
            log.info("Successfully saved {} news items to database", chunk.getItems().size());
            
        } catch (Exception e) {
            log.error("Error saving news to database: {}", e.getMessage());
            throw e;
        }
    }
}