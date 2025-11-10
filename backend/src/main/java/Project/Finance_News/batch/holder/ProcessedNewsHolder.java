package Project.Finance_News.batch.holder;

import Project.Finance_News.domain.News;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessedNewsHolder {
    private final List<News> processedNews = new ArrayList<>();

    public void addAll(List<? extends News> news) {
        processedNews.addAll(news);
    }

    public List<News> getAll() {
        return Collections.unmodifiableList(processedNews);
    }

    public void clear() {
        processedNews.clear();
    }
}
