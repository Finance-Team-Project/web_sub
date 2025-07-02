package Project.Finacne_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "terms")
public class Term {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String term;

    private Integer frequency;

    private String category;

    @Column(length = 2000)
    private String description;  // 용어 설명

    @Column(name = "created_at")
    private LocalDateTime lastUpdated;  // 마지막 업데이트 시간

    // === 연관관계 === //

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<Glossary> glossaries;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<UserVocabulary> userVocabularies;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<QuizTerm> quizTerms;
}
