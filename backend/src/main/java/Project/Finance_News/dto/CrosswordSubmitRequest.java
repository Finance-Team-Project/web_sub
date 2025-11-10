package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class CrosswordSubmitRequest {
    private Map<String, String> answers; // key: term(정답 단어), value: 사용자 입력
}


