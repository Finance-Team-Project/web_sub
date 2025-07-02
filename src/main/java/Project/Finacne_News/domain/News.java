package Project.Finacne_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String processedContent;  // 하이라이팅이 적용된 HTML 컨텐츠

    private String publisher;

    private String url; // 기사 원문 URL

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // 연관 관계 매핑
    @OneToMany(mappedBy = "news")
    private List<UserNewsLog> userNewsLogs;

    @OneToOne(mappedBy = "news", cascade = CascadeType.ALL)
    private NewsSummary newsSummary;

    @OneToMany(mappedBy = "news")
    private List<NewsKeyword> newsKeywords;
}