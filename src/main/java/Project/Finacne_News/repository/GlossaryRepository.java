package Project.Finacne_News.repository;

import Project.Finacne_News.domain.Glossary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlossaryRepository extends JpaRepository<Glossary, Long> {
}
