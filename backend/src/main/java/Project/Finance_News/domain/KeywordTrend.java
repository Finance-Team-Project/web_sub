package Project.Finance_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter @Setter
public class KeywordTrend {
    @Id
    private String keyword;

    private int frequency;

    @Column(name = "updated_at")
    private LocalTime updateAt;
}
