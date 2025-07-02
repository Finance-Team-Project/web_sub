/**
 * - 뉴스 관련 비즈니스 로직을 처리하는 서비스 계층
 * - 주요 기능:
 *   - saveNews(): 새로운 뉴스 저장
 *   - getAllNews(): 모든 뉴스 조회
 *   - getNewsById(): ID로 특정 뉴스 조회
 * - @Transactional 어노테이션으로 트랜잭션 관리
 * */

package Project.Finacne_News.service.news;

import Project.Finacne_News.domain.News;
import Project.Finacne_News.domain.Term;
import Project.Finacne_News.repository.NewsRepository;
import Project.Finacne_News.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final TermRepository termRepository;

    public Long saveNews(News news) {
        News savedNews = newsRepository.save(news);
        return savedNews.getId();
    }

    @Transactional(readOnly = true)
    public List<News> getAllNews() {
        return newsRepository.findAll();
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



}
