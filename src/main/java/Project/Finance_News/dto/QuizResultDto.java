package Project.Finance_News.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private Long quizId;
    private Long userId;
    private Integer score;
    private LocalDateTime takenAt;
    private Map<Long, Boolean> correctMap;
    // 제출 처리 후 서버 기준의 최신 총 포인트 (즉시 갱신용)
    private Integer totalPoints;
}