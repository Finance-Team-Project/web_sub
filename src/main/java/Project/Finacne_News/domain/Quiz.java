package Project.Finacne_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.security.Timestamp;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long id;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    // === 연관 관계 ===

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizTerm> quizTerms;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizResult> quizResults;
}

