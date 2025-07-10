package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuizItemDto {
    private Long termId;
    private String question; // 여러 해설이 하나의 문제로 제공
    private String initialHint;
    private int level;
}