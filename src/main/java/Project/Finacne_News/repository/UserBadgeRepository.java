package Project.Finacne_News.repository;

import Project.Finacne_News.domain.Badge;
import Project.Finacne_News.domain.User;
import Project.Finacne_News.domain.UserBadge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserBadgeRepository {

    @PersistenceContext
    private EntityManager em;

    // 이미 해당 유저가 해당 배지를 가지고 있는지 확인
    public boolean existsByUserAndBadge(User user, Badge badge) {
        Long count = em.createQuery("""
            SELECT COUNT(ub) FROM UserBadge ub 
            WHERE ub.user = :user AND ub.badge = :badge
            """, Long.class)
                .setParameter("user", user)
                .setParameter("badge", badge)
                .getSingleResult();

        return count != null && count > 0;
    }

    // 새 UserBadge 저장
    public void save(UserBadge userBadge) {
        em.persist(userBadge);
    }
}
