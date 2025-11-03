package Project.Finance_News.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class NewsUploadRequestDto {
    private String title;
    private String content;

    // camelCase 기본 + snake_case 수용
    private String imageUrl;
    @JsonProperty("image_url")
    private String imageUrlSnake;

    private String url;

    // 매체명
    private String press;

    // 다양한 날짜 입력 포맷 수용
    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy.MM.dd. a h:mm", locale = "ko")
    private LocalDateTime publishedAt;

    // 예: "2025-08-11T09:00:00Z"
    @JsonProperty("published_at")
    private String publishedAtIsoStr;

    // 대안 문자열 포맷
    @JsonProperty("date_str")
    private String publishedAtStr;

    // 선택 필드
    private String summary;

    private List<TermDto> terms;
    private List<String> keywords; // 추가: Python에서 보내는 keywords 필드 대응

    // 수신된 필드 중 사용 가능한 imageUrl 해석
    public String getEffectiveImageUrl() {
        if (imageUrl != null && !imageUrl.isBlank()) return imageUrl;
        if (imageUrlSnake != null && !imageUrlSnake.isBlank()) return imageUrlSnake;
        return null;
    }


    // 다양한 입력을 LocalDateTime으로 변환하는 메서드
    public LocalDateTime getPublishedAtFromString() {
        // ISO8601 우선 파싱
        if (publishedAtIsoStr != null && !publishedAtIsoStr.isEmpty()) {
            try {
                return java.time.OffsetDateTime.parse(publishedAtIsoStr)
                        .toLocalDateTime();
            } catch (Exception ignored) { }
        }
        // 기존 한글 포맷 파싱
        if (publishedAtStr != null && !publishedAtStr.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm", java.util.Locale.KOREAN);
                return LocalDateTime.parse(publishedAtStr, formatter);
            } catch (Exception ignored) { }
        }
        return publishedAt;
    }

    @Getter
    @Setter
    public static class TermDto {
        private String term;
        private String desc1;
        private String desc2;
        private String desc3;
    }
} 