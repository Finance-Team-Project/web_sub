package Project.Finance_News.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Getter
@Setter
public class NewsRequestDto {
    private String title;
    private String content;
    private String press;
    
    @JsonFormat(pattern = "yyyy.MM.dd. a h:mm", locale = "ko")
    private LocalDateTime publishedAt;
    
    // String으로 받는 대안 방법
    private String publishedAtStr;
    
    // String을 LocalDateTime으로 변환하는 메서드
    public LocalDateTime getPublishedAtFromString() {
        if (publishedAtStr != null && !publishedAtStr.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm", java.util.Locale.KOREAN);
            return LocalDateTime.parse(publishedAtStr, formatter);
        }
        return publishedAt;
    }
}
