package Project.Finacne_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "glossaries")
public class Glossary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "glossary_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(name = "short_def", columnDefinition = "TEXT")
    private String shortDefinition;

    @Column(name = "full_expl", columnDefinition = "TEXT")
    private String fullExplanation;

    @Column(name = "example_usage", columnDefinition = "TEXT")
    private String exampleUsage;
}
