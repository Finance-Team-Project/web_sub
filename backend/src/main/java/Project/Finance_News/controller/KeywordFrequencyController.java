package Project.Finance_News.controller;

import Project.Finance_News.domain.KeywordFrequency;
import Project.Finance_News.dto.KeywordFrequencyDto;
import Project.Finance_News.repository.KeywordFrequencyRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Map;

@RestController
@RequestMapping("/api/keyword-frequency")
public class KeywordFrequencyController {
    private final KeywordFrequencyRepository keywordFrequencyRepository;

    public KeywordFrequencyController(KeywordFrequencyRepository keywordFrequencyRepository) {
        this.keywordFrequencyRepository = keywordFrequencyRepository;
    }

    @GetMapping
    public Map<String, Object> getKeywordFrequencies() {
        List<KeywordFrequency> entities = keywordFrequencyRepository.findAll();
        int total = entities.stream().mapToInt(KeywordFrequency::getFrequency).sum();
        List<KeywordFrequencyDto> data = entities.stream()
                .map(e -> new KeywordFrequencyDto(e.getKeyword(), e.getFrequency()))
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())) // 빈도순 내림차순 정렬
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", data);
        return result;
    }
} 