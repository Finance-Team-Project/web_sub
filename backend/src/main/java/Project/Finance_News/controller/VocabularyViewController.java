package Project.Finance_News.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VocabularyViewController {
    @GetMapping("/vocabulary")
    public String vocabularyPage() {
        return "vocabulary";
    }
} 