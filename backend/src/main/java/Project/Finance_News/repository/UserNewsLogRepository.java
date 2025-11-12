package Project.Finance_News.repository;

import Project.Finance_News.domain.UserNewsLog;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNewsLogRepository extends JpaRepository<UserNewsLog, Long> {
    // 특정 사용자의 뉴스별 클릭 수 집계 (내림차순)
    @Query("SELECT unl.news.id, COUNT(unl) as cnt FROM UserNewsLog unl WHERE unl.user = :user GROUP BY unl.news.id ORDER BY cnt DESC")
    List<Object[]> findNewsClickCountByUser(@Param("user") User user);

    // 특정 사용자+뉴스의 클릭 수
    long countByUserAndNews(User user, News news);
    
    // 특정 사용자의 최근 본 뉴스 조회 (최신순, 중복 제거)
    @Query("SELECT unl FROM UserNewsLog unl WHERE unl.user = :user ORDER BY unl.viewedAt DESC")
    List<UserNewsLog> findByUserOrderByViewedAtDesc(@Param("user") User user);

    // 특정 사용자의 기록된 뉴스 삭제
    long deleteByUserAndNews(User user, News news);
} 