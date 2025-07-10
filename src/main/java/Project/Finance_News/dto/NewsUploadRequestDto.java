package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class NewsUploadRequestDto {
    private String title;
    private String content;
    private String imageUrl;
    private String url; 
    private List<TermDto> terms;

    @Getter
    @Setter
    public static class TermDto {
        private String term;
        private String description;
    }
} 