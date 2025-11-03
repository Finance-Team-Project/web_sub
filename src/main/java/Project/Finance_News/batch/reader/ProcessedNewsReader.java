package Project.Finance_News.batch.reader;

import Project.Finance_News.domain.News;
import Project.Finance_News.batch.holder.ProcessedNewsHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
public class ProcessedNewsReader implements ItemReader<News> {
    private final ProcessedNewsHolder holder;
    private Iterator<News> iterator;

    @Override
    public News read() {
        if (iterator == null) {
            iterator = holder.getAll().iterator();
        }
        
        if (iterator.hasNext()) {
            News news = iterator.next();
            log.debug("Reading processed news: {}", news.getTitle());
            return news;
        }
        
        return null;
    }
}
