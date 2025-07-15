package Project.Finance_News.repository;

import Project.Finance_News.domain.Glossary;
import Project.Finance_News.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlossaryRepository extends JpaRepository<Glossary, Long> {
    boolean existsByTermAndShortDefinition(Term term, String shortDefinition);
}
