package Project.Finacne_News.repository;

import Project.Finacne_News.domain.User;
import Project.Finacne_News.domain.UserPoint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserPointRepository {

    @PersistenceContext
    private EntityManager em;

    public UserPoint findByUser(User user) {
        return em.createQuery("""
            SELECT up FROM UserPoint up 
            WHERE up.user = :user
            """, UserPoint.class)
                .setParameter("user", user)
                .getResultStream() // 안전하게 첫 번째 결과만 반환
                .findFirst()
                .orElse(null);
    }
}
