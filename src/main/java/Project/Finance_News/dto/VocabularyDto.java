package Project.Finance_News.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyDto {
    private Integer id;
    private String term;
    private Long termId;
    private String description;
    private String category;
    private boolean starred;
    private Integer familiarityLevel;
    private String contextSentence;
    private String newsTitle;
    private String newsUrl;
    private String userNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 