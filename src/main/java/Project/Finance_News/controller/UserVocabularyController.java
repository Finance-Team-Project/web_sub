package Project.Finance_News.controller;

import Project.Finance_News.domain.User;
import Project.Finance_News.domain.UserVocabulary;
import Project.Finance_News.domain.Term;
import Project.Finance_News.repository.TermRepository;
import Project.Finance_News.repository.UserVocabularyRepository;
import Project.Finance_News.domain.session.SessionConst;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/vocabulary")
public class UserVocabularyController {

    @Autowired
    private UserVocabularyRepository userVocabularyRepository;
    @Autowired
    private TermRepository termRepository;

    @PostMapping("/add")
    public Map<String, Object> addToVocabulary(@RequestBody Map<String, Long> payload, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (user == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }
        Long termId = payload.get("termId");
        Term term = termRepository.findById(termId).orElse(null);
        if (term == null) {
            response.put("success", false);
            response.put("message", "용어를 찾을 수 없습니다.");
            return response;
        }
        // 이미 추가된 단어인지 확인
        if (userVocabularyRepository.existsByUserAndTerm(user, term)) {
            response.put("success", false);
            response.put("message", "이미 추가된 단어입니다.");
            return response;
        }
        UserVocabulary vocab = new UserVocabulary();
        vocab.setUser(user);
        vocab.setTerm(term);
        userVocabularyRepository.save(vocab);
        response.put("success", true);
        return response;
    }
} 