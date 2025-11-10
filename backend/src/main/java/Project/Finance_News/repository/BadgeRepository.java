package Project.Finance_News.repository;

import Project.Finance_News.domain.Badge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BadgeRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Badge> findAll() {
        return em.createQuery("SELECT b FROM Badge b", Badge.class)
                .getResultList();
    }

    // (선택) findById 같은 추가 메서드도 원한다면 여기에 추가할 수 있음
    public Badge findById(Long id) {
        return em.find(Badge.class, id);
    }

    public void save(Badge badge) {
        em.persist(badge);
    }
    
    public long count() {
        return em.createQuery("SELECT COUNT(b) FROM Badge b", Long.class)
                .getSingleResult();
    }
}
