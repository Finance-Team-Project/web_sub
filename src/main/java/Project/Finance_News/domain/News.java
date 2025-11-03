package Project.Finance_News.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "press")
    private String press;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, length = 512, unique = true)
    private String url;

    private String imageUrl;

    // 연관 관계 매핑
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserNewsLog> userNewsLogs = new ArrayList<>();

    @OneToOne(mappedBy = "news", cascade = CascadeType.ALL)
    private NewsSummary newsSummary;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL)
    @Builder.Default
    private List<NewsKeyword> newsKeywords = new ArrayList<>();

    @Transient  // DB에 저장되지 않는 임시 필드
    private List<String> keywords;

    // 연관관계 편의 메서드
    public void addKeyword(String keyword) {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        keywords.add(keyword);
    }

    public void addNewsKeyword(NewsKeyword newsKeyword) {
        newsKeywords.add(newsKeyword);
        newsKeyword.setNews(this);
    }
}