package Project.Finacne_News.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private Long quizId;
    private Long userId;
    private Integer score;
    private LocalDateTime takenAt;
}