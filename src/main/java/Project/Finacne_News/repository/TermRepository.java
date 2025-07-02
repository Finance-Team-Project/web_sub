package Project.Finacne_News.repository;

import Project.Finacne_News.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
