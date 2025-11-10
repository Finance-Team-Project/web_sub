/**
 * - 클라이언트로부터 뉴스 생성 요청을 받을 때 사용하는 DTO
 * - 포함 필드:
 *   - title: 뉴스 제목
 *   - content: 뉴스 내용
 *   - publisher: 발행자
 *   - publishedAt: 발행 시간
 * */

package Project.Finance_News.repository;

import Project.Finance_News.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    boolean existsByTitleAndPublishedAt(String title, LocalDateTime publishedAt);
    boolean existsByUrl(String url);
    java.util.Optional<News> findByUrl(String url);

    @Query("SELECT n FROM News n JOIN n.newsKeywords nk WHERE nk.keyword = :keyword")
    Page<News> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
