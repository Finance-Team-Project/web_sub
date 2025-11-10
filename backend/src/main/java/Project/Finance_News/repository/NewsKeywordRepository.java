package Project.Finance_News.repository;

import Project.Finance_News.domain.News;
import Project.Finance_News.domain.NewsKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsKeywordRepository extends JpaRepository<NewsKeyword, Long> {
    // 필요시 커스텀 쿼리 추가 가능
    @Query("SELECT nk.keyword FROM NewsKeyword nk WHERE nk.news.id = :newsId")
    List<String> findKeywordsByNewsId(@Param("newsId") Long newsId);
    
    List<NewsKeyword> findByNews(News news);
}
