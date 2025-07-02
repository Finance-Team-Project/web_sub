package Project.Finacne_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String nickname;

    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    // User - UserVocabulary
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVocabulary> userVocabularies;

    // User - UserNewsLog
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserNewsLog> newsLogs;

    // User - QuizResult
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizResult> quizResults;

    // User - UserPoint
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPoint> userPoints;

    // User - UserBadge
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> userBadges;

    // User - UserInterestCategory
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInterestCategory> interestCategories;

}