/**
 * - 클라이언트로부터 뉴스 생성 요청을 받을 때 사용하는 DTO
 * - 포함 필드:
 *   - title: 뉴스 제목
 *   - content: 뉴스 내용
 *   - publisher: 발행자
 *   - publishedAt: 발행 시간
 * */

package Project.Finacne_News.repository;

import Project.Finacne_News.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
}
