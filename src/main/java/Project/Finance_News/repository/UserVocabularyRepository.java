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
}
