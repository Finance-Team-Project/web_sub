package Project.Finacne_News.repository;

import Project.Finacne_News.domain.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // 특정 사용자의 퀴즈 결과 조회
    List<QuizResult> findByUserId(Long userId);

    // 특정 퀴즈에 대한 모든 결과 조회
    List<QuizResult> findByQuizId(Long quizId);

    // 사용자 + 퀴즈 기준 단일 결과 조회
    QuizResult findByUserIdAndQuizId(Long userId, Long quizId);
}
