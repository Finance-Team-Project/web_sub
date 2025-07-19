package Project.Finance_News.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class KeywordFrequency {
    @Id
    private String keyword;
    private int frequency;
} 