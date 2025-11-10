package Project.Finance_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class NewsSummary {
    @Id
    @Column(name = "news_id")
    private Long newsid;

    @OneToOne
    @JoinColumn(name = "news_id")
    private News news;

    private String summary;
}
