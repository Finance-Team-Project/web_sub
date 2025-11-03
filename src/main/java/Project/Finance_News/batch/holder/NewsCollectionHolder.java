package Project.Finance_News.batch.holder;

import Project.Finance_News.dto.NewsDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class NewsCollectionHolder {
    private final List<NewsDto> newsCollection = new ArrayList<>();

    public void addAll(List<NewsDto> news) {
        newsCollection.addAll(news);
    }

    public List<NewsDto> getAll() {
        return Collections.unmodifiableList(newsCollection);
    }

    public void clear() {
        newsCollection.clear();
    }
}
