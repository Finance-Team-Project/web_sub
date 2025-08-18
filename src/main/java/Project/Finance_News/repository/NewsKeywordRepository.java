package Project.Finance_News.repository;

import Project.Finance_News.domain.NewsKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsKeywordRepository extends JpaRepository<NewsKeyword, Long> {
    // 필요시 커스텀 쿼리 추가 가능
} 