package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class CrosswordResultDto {
    private int score;
    private int totalPoints;
    private Map<String, Boolean> results; // key: term(정답 단어), value: 정답 여부
}


