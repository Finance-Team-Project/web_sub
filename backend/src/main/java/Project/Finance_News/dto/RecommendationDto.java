package Project.Finance_News.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class RecommendationDto {
    @JsonProperty("news_id")
    private Long newsId;
    private String title;
    private String url;
    @JsonProperty("matched_keywords")
    private List<String> matchedKeywords;
}
