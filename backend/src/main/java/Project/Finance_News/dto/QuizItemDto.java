package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuizItemDto {
    private Long termId;
    private String question; // 여러 해설이 하나의 문제로 제공
    private String initialHint;
    private int level;
    private String term; // 정답 단어(크로스워드용)
    private int row;     // 시작 행
    private int col;     // 시작 열
    private String direction; // "across" 또는 "down"
    private int number;  // 문제 번호
    private int length;  // 단어 길이
    private String clue; // 해설(문제)
}