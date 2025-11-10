package Project.Finance_News.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class RecommendRequestDto {
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("clicked_news_ids")
    private List<Long> clickedNewsIds;
}
