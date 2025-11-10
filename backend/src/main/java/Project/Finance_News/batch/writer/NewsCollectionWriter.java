package Project.Finance_News.batch.writer;

import Project.Finance_News.dto.NewsApiResponse;
import Project.Finance_News.dto.NewsDto;
import Project.Finance_News.batch.holder.NewsCollectionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class NewsCollectionWriter implements ItemWriter<NewsApiResponse> {
    private final NewsCollectionHolder holder;

    @Override
    public void write(Chunk<? extends NewsApiResponse> chunk) {
        List<? extends NewsApiResponse> items = chunk.getItems();
        if (items.isEmpty()) {
            log.warn("No news items to write");
            return;
        }

        NewsApiResponse response = items.get(0);
        if (response.getNews() != null && !response.getNews().isEmpty()) {
            holder.addAll(response.getNews());
            log.info("Stored {} news items in holder", response.getNews().size());
        } else {
            log.warn("Received response but news list is empty or null");
        }
    }
}