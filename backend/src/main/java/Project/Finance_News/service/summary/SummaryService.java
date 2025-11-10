package Project.Finance_News.service.summary;

import Project.Finance_News.domain.News;
import Project.Finance_News.domain.User;
import Project.Finance_News.repository.NewsRepository;
import Project.Finance_News.repository.UserRepository;
import Project.Finance_News.service.prompt.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final PromptService promptService;
    private final HyperClovaClient hyperClovaClient;

    /**
     * 뉴스와 사용자 정보를 바탕으로 프롬프트를 생성하고
     * AI 요약 결과를 반환하는 핵심 서비스 로직
     *
     * @param newsId 요약할 뉴스의 ID
     * @param userId 요약을 요청한 사용자 ID
     * @return 사용자 맞춤형 요약 결과 문자열
     */
    public String summarize(Long newsId, Long userId) {
        // 1. 뉴스 내용 가져오기
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스가 존재하지 않습니다."));

        // 2. 사용자 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 3. 프롬프트 생성
        String prompt = promptService.buildPrompt(news.getContent(), user);

        // 4. 요약 결과 생성 (AI 호출)
        return hyperClovaClient.getSummary(prompt);
    }

    /**
     * chat completions 기반 맞춤형 요약 서비스
     * @param newsId 요약할 뉴스의 ID
     * @param userId 요약을 요청한 사용자 ID
     * @return 맞춤형 챗봇 요약 결과
     */
    public String summarizeWithChat(Long newsId, Long userId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스가 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // system, user 역할 메시지 구성
        String systemPrompt = "당신은 경제 뉴스를 사용자의 직업과 목적에 맞게 쉽게 요약해주는 AI입니다.";
        String userPrompt = String.format(
                "당신은 사용자의 직업과 목표에 맞는 정보를 요약하고 해석해주는 전문가입니다.\n\n" +
                        "직업: %s\n" +
                        "목표: %s\n\n" +
                        "뉴스 기사:\n%s\n\n" +
                        "1. 위 뉴스 기사를 사용자의 직업과 목표를 고려하여 2~3문장으로 요약해 주세요.\n" +
                        "2. 이어서, 이 뉴스가 사용자의 목표 달성에 어떤 의미나 영향을 주는지를 3~4문장으로 분석해 주세요.\n" +
                        "3. 반드시 사용자의 관점에서 구체적이고 현실적인 언어로 해석해 주세요.",
                user.getJob(), user.getGoal(), news.getContent()
        );
        var messages = java.util.List.of(
                java.util.Map.of("role", "system", "content", systemPrompt),
                java.util.Map.of("role", "user", "content", userPrompt)
        );
        return hyperClovaClient.getChatCompletion(messages);
    }
}
