package Project.Finance_News.repository;

import Project.Finance_News.domain.Quiz;
import Project.Finance_News.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 특정 사용자가 푼 퀴즈 수 반환
    long countByUser(User user);
}
