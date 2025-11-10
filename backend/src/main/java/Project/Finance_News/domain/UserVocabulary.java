package Project.Finance_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "user_vocabulary")
public class UserVocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_vocabulary_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(name = "familiarity_level")
    private Integer familiarityLevel;

    @Column(name = "last_seen")
    private LocalDate lastSeen;

    @Column(name = "starred", nullable = false)
    private boolean starred = false;

    @Column(name = "context_sentence", length = 1000)
    private String contextSentence;

    @Column(name = "news_title")
    private String newsTitle;

    @Column(name = "news_url")
    private String newsUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
