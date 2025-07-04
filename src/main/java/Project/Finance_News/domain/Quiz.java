package Project.Finance_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String type; // "short_answer", "crossword" 등 퀴즈 타입 구분

    // === 연관 관계 ===

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizTerm> quizTerms;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizResult> quizResults;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

