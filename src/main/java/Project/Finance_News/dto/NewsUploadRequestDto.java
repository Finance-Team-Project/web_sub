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
    private List<String> keywords; // 추가: Python에서 보내는 keywords 필드 대응

    @Getter
    @Setter
    public static class TermDto {
        private String term;
        private String desc1;
        private String desc2;
        private String desc3;
    }
} 