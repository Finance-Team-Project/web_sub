package Project.Finacne_News.domain;

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

    private String description;

    // 역방향 매핑 (UserBadge → Badge)
    @OneToMany(mappedBy = "badge")
    private List<UserBadge> userBadges;
}