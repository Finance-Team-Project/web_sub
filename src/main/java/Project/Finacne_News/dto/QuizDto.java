package Project.Finacne_News.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class QuizDto {
    private Long quizId;
    private String type;
    private List<QuizItemDto> items;
}