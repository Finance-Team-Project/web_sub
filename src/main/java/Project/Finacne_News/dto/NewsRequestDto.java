package Project.Finacne_News.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.security.Timestamp;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class NewsRequestDto {
    private String title;
    private String content;
    private String publisher;
    private LocalDateTime publishedAt;
    private String url;
}
