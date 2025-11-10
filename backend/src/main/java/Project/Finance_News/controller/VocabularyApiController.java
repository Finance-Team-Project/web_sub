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
import java.util.Comparator;
import java.util.stream.Collectors;
import Project.Finance_News.repository.NewsRepository;
import Project.Finance_News.domain.News;
import Project.Finance_News.service.news.SentenceExtractor;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyApiController {
    @Autowired
    private UserVocabularyRepository userVocabularyRepository;
    @Autowired
    private TermRepository termRepository;
    @Autowired
    private NewsRepository newsRepository;

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToVocabulary(@RequestBody Map<String, Object> payload, HttpSession session) {
        System.out.println("[단어장추가] payload: " + payload);
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        Long termId = ((Number)payload.get("termId")).longValue();
        Long newsId = payload.get("newsId") != null ? ((Number)payload.get("newsId")).longValue() : null;
        System.out.println("[단어장추가] newsId: " + newsId);
        String newsTitle = (String) payload.getOrDefault("newsTitle", "");
        String newsUrl = (String) payload.getOrDefault("newsUrl", "");
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
        String contextSentence = "";
        if (newsId != null) {
            News news = newsRepository.findById(newsId).orElse(null);
            if (news != null) {
                // 로그 추가: 실제 본문과 용어 값 확인
                System.out.println("[단어장추가] 뉴스 본문: " + news.getContent());
                System.out.println("[단어장추가] 용어: " + term.getTerm());
                contextSentence = SentenceExtractor.extractSentenceWithWord(news.getContent(), term.getTerm());
                System.out.println("추출된 문장" + contextSentence);
            }
        }

        System.out.println("추출된 문장" + contextSentence);
        UserVocabulary vocab = new UserVocabulary();
        vocab.setUser(user);
        vocab.setTerm(term);
        vocab.setContextSentence(contextSentence);
        vocab.setNewsTitle(newsTitle);
        vocab.setNewsUrl(newsUrl);
        userVocabularyRepository.save(vocab);
        response.put("success", true);
        return response;
    }

    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getVocabularyList(@RequestParam(value = "search", required = false) String search, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        var vocabList = userVocabularyRepository.findByUserId(user.getId());
        // 검색어 필터링
        if (search != null && !search.isBlank()) {
            vocabList = vocabList.stream()
                .filter(uv -> uv.getTerm().getTerm().contains(search))
                .collect(Collectors.toList());
        }
        // starred 우선, 사전순 정렬
        vocabList = vocabList.stream()
            .sorted(Comparator.comparing(UserVocabulary::isStarred).reversed()
                .thenComparing(uv -> uv.getTerm().getTerm()))
            .collect(Collectors.toList());
        response.put("success", true);
        response.put("vocabularies", vocabList.stream().map(uv -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", uv.getId());
            map.put("term", uv.getTerm().getTerm());
            map.put("termId", uv.getTerm().getId());
            map.put("starred", uv.isStarred());
            return map;
        }).toList());
        return response;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Map<String, Object> getVocabularyDetail(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        var uvOpt = userVocabularyRepository.findById(id);
        if (uvOpt.isEmpty() || !uvOpt.get().getUser().getId().equals(user.getId())) {
            response.put("success", false);
            response.put("message", "해당 단어를 찾을 수 없습니다.");
            return response;
        }
        var uv = uvOpt.get();
        Map<String, Object> vocabDetail = new HashMap<>();
        vocabDetail.put("id", uv.getId());
        vocabDetail.put("term", uv.getTerm().getTerm());
        vocabDetail.put("description", uv.getTerm().getDescription());
        vocabDetail.put("contextSentence", uv.getContextSentence());
        response.put("success", true);
        response.put("vocabulary", vocabDetail);
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public Map<String, Object> deleteVocabulary(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        var uvOpt = userVocabularyRepository.findById(id);
        if (uvOpt.isEmpty() || !uvOpt.get().getUser().getId().equals(user.getId())) {
            response.put("success", false);
            response.put("message", "해당 단어를 찾을 수 없습니다.");
            return response;
        }
        userVocabularyRepository.deleteById(id);
        response.put("success", true);
        return response;
    }

    // 별표 토글 API
    @PostMapping("/star/{id}")
    @ResponseBody
    public Map<String, Object> toggleStarred(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        var uvOpt = userVocabularyRepository.findById(id);
        if (uvOpt.isEmpty() || !uvOpt.get().getUser().getId().equals(user.getId())) {
            response.put("success", false);
            response.put("message", "해당 단어를 찾을 수 없습니다.");
            return response;
        }
        var uv = uvOpt.get();
        uv.setStarred(!uv.isStarred());
        userVocabularyRepository.save(uv);
        response.put("success", true);
        response.put("starred", uv.isStarred());
        return response;
    }
}