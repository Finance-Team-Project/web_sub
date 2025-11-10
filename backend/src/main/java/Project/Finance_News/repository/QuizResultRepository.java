package Project.Finance_News.repository;

import Project.Finance_News.domain.QuizResult;
import Project.Finance_News.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    // 특정 사용자의 퀴즈 결과 조회
    List<QuizResult> findByUserId(Long userId);

    // 특정 퀴즈에 대한 모든 결과 조회
    List<QuizResult> findByQuizId(Long quizId);

    // 사용자 + 퀴즈 기준 단일 결과 조회
    QuizResult findByUserIdAndQuizId(Long userId, Long quizId);

    // 정답 맞춘 이력이 있는지 확인
    boolean existsByUserAndIsCorrectTrue(User user);

    // 정답 맞춘 개수 반환
    long countByUserAndIsCorrectTrue(User user);
}
