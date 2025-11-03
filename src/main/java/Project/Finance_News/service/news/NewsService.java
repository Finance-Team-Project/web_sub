/**
 * - 뉴스 관련 비즈니스 로직을 처리하는 서비스 계층
 * - 주요 기능:
 *   - saveNews(): 새로운 뉴스 저장
 *   - getAllNews(): 모든 뉴스 조회
 *   - getNewsById(): ID로 특정 뉴스 조회
 * - @Transactional 어노테이션으로 트랜잭션 관리
 * */

package Project.Finance_News.service.news;

import Project.Finance_News.domain.Glossary;
import Project.Finance_News.domain.News;
import Project.Finance_News.domain.NewsKeyword;
import Project.Finance_News.domain.Term;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.UserNewsLog;
import Project.Finance_News.domain.KeywordFrequency;
import java.time.LocalDateTime;
import Project.Finance_News.repository.GlossaryRepository;
import Project.Finance_News.repository.NewsKeywordRepository;
import Project.Finance_News.repository.NewsRepository;
import Project.Finance_News.repository.TermRepository;
import Project.Finance_News.repository.UserNewsLogRepository;
import Project.Finance_News.repository.KeywordFrequencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Project.Finance_News.dto.TermDto;
import Project.Finance_News.dto.NewsResponseDto;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private final TermRepository termRepository;
    private final UserNewsLogRepository userNewsLogRepository;
    private final GlossaryRepository glossaryRepository;
    private final NewsKeywordRepository newsKeywordRepository;
    private final KeywordFrequencyRepository keywordFrequencyRepository;

    public Long saveNews(News news) {
        // 중복 뉴스 체크: url이 같은 뉴스가 있으면 기존 엔티티 ID 반환(멱등성)
        if (newsRepository.existsByUrl(news.getUrl())) {
            return newsRepository.findByUrl(news.getUrl())
                    .map(News::getId)
                    .orElse(null);
        }
        News savedNews = newsRepository.save(news);
        return savedNews.getId();
    }

    /**
     * 뉴스와 키워드 리스트를 함께 저장
     */
    public Long saveNewsWithKeywords(News news, List<String> keywords) {
        Long newsId = saveNews(news);
        if (newsId != null && keywords != null) {
            News savedNews = newsRepository.findById(newsId).orElse(null);
            if (savedNews != null) {
                for (String keyword : keywords) {
                    if (keyword != null && !keyword.isBlank()) {
                        NewsKeyword newsKeyword = new NewsKeyword();
                        newsKeyword.setNews(savedNews);
                        newsKeyword.setKeyword(keyword);
                        newsKeywordRepository.save(newsKeyword);
                    }
                }
                // 키워드 빈도 증가
                increaseKeywordFrequency(keywords);
            }
        }
        return newsId;
    }

    @Transactional(readOnly = true)
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<News> getNewsPaged(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<News> getNewsById(Long id) {
        return newsRepository.findById(id);
    }

    public News getNewsWithHighlight(Long id) {
        News news = newsRepository.findById(id).orElseThrow();
        news.setContent(highlightTerms(news.getContent()));
        return news;
    }

    private String highlightTerms(String content) {
        // 기존 <mark> 제거 (중복 방지)
        content = content.replaceAll("(?i)</?mark>", "");

        List<Term> terms = termRepository.findAll();
        for (Term term : terms) {
            String keyword = Pattern.quote(term.getTerm());

            // 조사 포함 가능: "금리를", "금리의" 등을 포함
            String regex = "(" + keyword + ")([가-힣]{0,2})?";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb,
                        "<mark>" + matcher.group(1) + "</mark>" +
                                (matcher.group(2) != null ? matcher.group(2) : ""));
            }
            matcher.appendTail(sb);
            content = sb.toString();
        }
        return content;
    }

    public void saveOrUpdateTerm(String term, String description) {
        if (!termRepository.existsByTerm(term)) {
            Term newTerm = new Term();
            newTerm.setTerm(term);
            newTerm.setDescription(description);
            termRepository.save(newTerm);
        }
    }

    /**
     * 파이썬 서버에서 받은 terms 배열을 저장 (각 desc1~desc3을 Glossary로 생성)
     */
    public void saveTermsAndGlossaries(List<TermDto> terms) {
        for (TermDto termDto : terms) {
            // 1. term이 DB에 없으면 생성, 있으면 가져옴
            Term term = termRepository.findByTerm(termDto.getTerm())
                    .orElseGet(() -> {
                        Term t = new Term();
                        t.setTerm(termDto.getTerm());
                        return termRepository.save(t);
                    });

            // --- description 조합 및 저장 ---
            StringBuilder descBuilder = new StringBuilder();
            int idx = 1;
            if (termDto.getDesc1() != null && !termDto.getDesc1().isBlank()) {
                descBuilder.append(idx++).append(". ").append(termDto.getDesc1()).append("\n");
            }
            if (termDto.getDesc2() != null && !termDto.getDesc2().isBlank()) {
                descBuilder.append(idx++).append(". ").append(termDto.getDesc2()).append("\n");
            }
            if (termDto.getDesc3() != null && !termDto.getDesc3().isBlank()) {
                descBuilder.append(idx++).append(". ").append(termDto.getDesc3()).append("\n");
            }
            String descResult = descBuilder.toString().trim();
            if (!descResult.isEmpty()) {
                term.setDescription(descResult);
                termRepository.save(term); // 업데이트 반영
            }

            // 2. desc1~desc3(빈 값 제외) 각각 Glossary 생성, term과 연결
            if (termDto.getDesc1() != null && !termDto.getDesc1().isBlank()) {
                if (!glossaryRepository.existsByTermAndShortDefinition(term, termDto.getDesc1())) {
                    Glossary glossary1 = new Glossary();
                    glossary1.setTerm(term);
                    glossary1.setShortDefinition(termDto.getDesc1());
                    glossaryRepository.save(glossary1);
                }
            }
            if (termDto.getDesc2() != null && !termDto.getDesc2().isBlank()) {
                if (!glossaryRepository.existsByTermAndShortDefinition(term, termDto.getDesc2())) {
                    Glossary glossary2 = new Glossary();
                    glossary2.setTerm(term);
                    glossary2.setShortDefinition(termDto.getDesc2());
                    glossaryRepository.save(glossary2);
                }
            }
            if (termDto.getDesc3() != null && !termDto.getDesc3().isBlank()) {
                if (!glossaryRepository.existsByTermAndShortDefinition(term, termDto.getDesc3())) {
                    Glossary glossary3 = new Glossary();
                    glossary3.setTerm(term);
                    glossary3.setShortDefinition(termDto.getDesc3());
                    glossaryRepository.save(glossary3);
                }
            }
        }
    }

    /**
     * 사용자별 뉴스 클릭수(내림차순)로 뉴스 리스트 반환
     */
    @Transactional(readOnly = true)
    public List<News> getUserInterestNewsByClickCount(User user, int limit) {
        List<Object[]> result = userNewsLogRepository.findNewsClickCountByUser(user);
        List<Long> newsIds = result.stream()
                .map(obj -> (Long) obj[0])
                .limit(limit)
                .toList();
        if (newsIds.isEmpty()) return List.of();
        return newsRepository.findAllById(newsIds);
    }

    public void increaseKeywordFrequency(List<String> keywords) {
        for (String keyword : keywords) {
            KeywordFrequency kf = keywordFrequencyRepository.findById(keyword)
                .orElse(new KeywordFrequency());
            kf.setKeyword(keyword);
            kf.setFrequency(kf.getFrequency() + 1);
            keywordFrequencyRepository.save(kf);
        }
    }

    @Transactional(readOnly = true)
    public Optional<News> findById(Long id) {
        return newsRepository.findById(id);
    }

    /**
     * 사용자의 뉴스 클릭을 로그에 기록
     */
    @Transactional
    public void logNewsClick(User user, News news) {
        UserNewsLog newsLog = new UserNewsLog();
        newsLog.setUser(user);
        newsLog.setNews(news);
        newsLog.setViewedAt(LocalDateTime.now());
        userNewsLogRepository.save(newsLog);
        log.info("뉴스 클릭 로그 저장 - 사용자: {}, 뉴스: {}", user.getId(), news.getId());
    }

    public void decreaseKeywordFrequency(List<String> keywords) {
        for (String keyword : keywords) {
            keywordFrequencyRepository.findById(keyword).ifPresent(kf -> {
                kf.setFrequency(Math.max(0, kf.getFrequency() - 1));
                keywordFrequencyRepository.save(kf);
            });
        }
    }

    public List<KeywordFrequency> getTopKeywords(int n) {
        return keywordFrequencyRepository.findAll(org.springframework.data.domain.PageRequest.of(0, n, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "frequency"))).getContent();
    }

    @Transactional(readOnly = true)
    public List<String> getTopKeywords(Long newsId) {
        return newsKeywordRepository.findKeywordsByNewsId(newsId)
                .stream().limit(5).toList();
    }

    @Transactional(readOnly = true)
    public Page<NewsResponseDto> findByKeyword(String keyword, Pageable pageable) {
        return newsRepository.findByKeyword(keyword, pageable)
                .map(news -> NewsResponseDto.builder()
                        .id(news.getId())
                        .title(news.getTitle())
                        .content(news.getContent())
                        .press(news.getPress())
                        .publishedAt(news.getPublishedAt())
                        .imageUrl(news.getImageUrl())
                        .url(news.getUrl())
                        .build());
    }

}
