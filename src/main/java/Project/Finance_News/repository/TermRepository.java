package Project.Finance_News.repository;

import Project.Finance_News.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {
    /**
     * 용어검색 기능 (단어로 search)
     * */
    Optional<Term> findByTerm(String word);
    List<Term> findByTermIn(Collection<String> words);
    boolean existsByTerm(String word);
    List<Term> findByCategory(String category);
    // 기존: 글자+frequency로 찾기
    // List<Term> findByCharAndFrequency(char ch, int frequency);

    // frequency 상관 없이 글자만으로 찾기
    @Query("SELECT t FROM Term t WHERE t.term LIKE %:ch%")
    List<Term> findByChar(@Param("ch") char ch);
}
