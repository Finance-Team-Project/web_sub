package Project.Finance_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;  // 마지막 업데이트 시간

}
