package Project.Finance_News.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(name = "interest_category")
public class InterestCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_category_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // 역방향 매핑
    @OneToMany(mappedBy = "interestCategory")
    private List<UserInterestCategory> userInterestCategories;
}
