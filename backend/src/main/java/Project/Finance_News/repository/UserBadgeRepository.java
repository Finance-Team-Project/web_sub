package Project.Finance_News.repository;

import Project.Finance_News.domain.Badge;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.UserBadge;
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

    // 새 UserBadge 저장 (detached 방지: merge 사용)
    public void save(UserBadge userBadge) {
        if (userBadge.getId() == null) {
            Integer nextId = em.createQuery("""
                SELECT COALESCE(MAX(ub.id), 0) + 1 FROM UserBadge ub
            """, Integer.class).getSingleResult();
            userBadge.setId(nextId);
        }
        em.merge(userBadge);
    }

    // 특정 유저가 획득한 모든 뱃지 반환
    public java.util.List<UserBadge> findByUser(User user) {
        return em.createQuery("""
            SELECT ub FROM UserBadge ub WHERE ub.user = :user
        """, UserBadge.class)
            .setParameter("user", user)
            .getResultList();
    }
}
