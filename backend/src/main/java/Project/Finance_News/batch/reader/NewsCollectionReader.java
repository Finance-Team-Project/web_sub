package Project.Finance_News.batch.reader;

import Project.Finance_News.dto.NewsDto;
import Project.Finance_News.batch.holder.NewsCollectionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
public class NewsCollectionReader implements ItemReader<NewsDto> {
    private final NewsCollectionHolder holder;
    private Iterator<NewsDto> iterator;

    @Override
    public NewsDto read() {
        if (iterator == null) {
            iterator = holder.getAll().iterator();
        }
        
        if (iterator.hasNext()) {
            NewsDto newsDto = iterator.next();
            log.debug("Reading news: {}", newsDto.getTitle());
            return newsDto;
        }
        
        return null;
    }
}
