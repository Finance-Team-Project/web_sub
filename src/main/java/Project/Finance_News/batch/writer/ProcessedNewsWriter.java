package Project.Finance_News.batch.writer;

import Project.Finance_News.domain.News;
import Project.Finance_News.batch.holder.ProcessedNewsHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class ProcessedNewsWriter implements ItemWriter<News> {
    private final ProcessedNewsHolder holder;

    @Override
    public void write(Chunk<? extends News> chunk) {
        if (chunk.isEmpty()) {
            log.warn("No processed news items to write");
            return;
        }

        holder.addAll(chunk.getItems());
        log.info("Stored {} processed news items in holder", chunk.getItems().size());
    }
}