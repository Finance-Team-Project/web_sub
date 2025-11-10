package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NewsDto {
    private String title;
    private String content;
    private String url;
    private String imageUrl;
    @com.fasterxml.jackson.annotation.JsonProperty(value = "press")
    @com.fasterxml.jackson.annotation.JsonAlias({"publisher", "source"})
    private String press;
    private LocalDateTime publishedAt;
    private List<String> keywords;
}
