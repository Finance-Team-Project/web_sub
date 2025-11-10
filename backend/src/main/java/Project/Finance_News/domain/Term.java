package Project.Finance_News.domain;

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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;  // 마지막 업데이트 시간

    // === 연관관계 === //
    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<Glossary> glossaries;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<UserVocabulary> userVocabularies;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<QuizTerm> quizTerms;
}
