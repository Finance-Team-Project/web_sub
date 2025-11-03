package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class NewsApiResponse {
    private List<NewsDto> news;
    private String status;
    private String message;
}
