package Project.Finance_News.repository;

import Project.Finance_News.domain.KeywordFrequency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordFrequencyRepository extends JpaRepository<KeywordFrequency, String> {
} 