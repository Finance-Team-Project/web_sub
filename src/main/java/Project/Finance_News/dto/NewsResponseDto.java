/**
 * - 클라이언트에게 뉴스 정보를 반환할 때 사용하는 DTO
 * - @Builder 패턴 적용으로 객체 생성 용이
 * - NewsRequestDto와 비슷하지만 id 필드가 추가됨
 * */

package Project.Finance_News.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponseDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private String imageUrl;
    private String url;
    private String press;
}
