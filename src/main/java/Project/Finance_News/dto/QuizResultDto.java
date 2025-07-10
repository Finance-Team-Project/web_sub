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
}