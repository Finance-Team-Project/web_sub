package Project.Finance_News.repository;

import Project.Finance_News.domain.UserVocabulary;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVocabularyRepository extends JpaRepository<UserVocabulary, Integer> {

    // userId로 uservoca 조회
    List<UserVocabulary> findByUserId(Long userId);

    // user와 term으로 중복 체크
    boolean existsByUserAndTerm(User user, Term term);

    UserVocabulary user(User user);

    // 최근 등록된 7개 단어
    List<UserVocabulary> findTop7ByUserIdOrderByCreatedAtDesc(Long userId);

    // 최근 등록된 10개 단어
    List<UserVocabulary> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    // 최근 등록된 5개 단어
    List<UserVocabulary> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    UserVocabulary findTopByUserIdOrderByCreatedAtDesc(Long userId);

    // 정답 처리 시 단어장을 비우기 위해 사용자와 용어로 삭제
    void deleteByUserAndTerm(User user, Term term);
}
