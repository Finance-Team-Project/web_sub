package Project.Finance_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(name = "badges")
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private BadgeType type;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    // 역방향 매핑 (UserBadge → Badge)
    @OneToMany(mappedBy = "badge")
    private List<UserBadge> userBadges;

    private int conditionValue;

}