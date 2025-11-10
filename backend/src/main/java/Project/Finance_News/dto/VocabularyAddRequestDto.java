package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyAddRequestDto {
    private Long termId;
    private String contextSentence;
    private String newsTitle;
    private String newsUrl;
} 