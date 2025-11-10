package Project.Finance_News.dto;

import Project.Finance_News.domain.News;
import Project.Finance_News.domain.NewsKeyword;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor
public class NewsCacheRequestDto {
    @JsonProperty("news_id")
    private Long newsId;
    private String title;
    private String url;
    private List<String> keywords;

    public static NewsCacheRequestDto from(News news, List<NewsKeyword> newsKeywords) {
        NewsCacheRequestDto dto = new NewsCacheRequestDto();
        dto.setNewsId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setUrl(news.getUrl());
        dto.setKeywords(newsKeywords.stream()
                .map(NewsKeyword::getKeyword)
                .collect(Collectors.toList()));
        return dto;
    }
}
