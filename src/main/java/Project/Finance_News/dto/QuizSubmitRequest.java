package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class QuizSubmitRequest {

    private Long quizId;              // 어떤 퀴즈를 제출하는지
    private Long userId;              // 누가 제출하는지
    private Map<Long, String> answers; // <QuizTermId, 사용자의 정답>
}
