package Project.Finance_News.service.prompt;

import Project.Finance_News.domain.User;
import org.springframework.stereotype.Service;

/**
 * 뉴스 요약 요청을 위한 사용자 맞춤형 프롬프트를 생성하는 클래스
 */
@Service
public class PromptService {

    /**
     * 사용자 정보와 뉴스 본문을 바탕으로 AI 요약 프롬프트를 생성
     *
     * @param newsContent 요약할 뉴스 본문
     * @param user        사용자 정보 (직업, 목표 등 포함)
     * @return 생성된 프롬프트 문자열
     */
    public String buildPrompt(String newsContent, User user) {
        String job = user.getJob();     // 예: 대학생, 자영업자
        String goal = user.getGoal();   // 예: 금융 취업, 투자 판단

        return """
            다음은 경제 뉴스 기사입니다.
            사용자는 '%s' 직업을 가진 사람이며, '%s'을(를) 목표로 하고 있습니다.
            이 사용자가 이해하기 쉬운 수준으로 뉴스 내용을 2~3문장으로 요약해주세요.

            기사 본문:
            %s

            요약:
            """.formatted(job, goal, newsContent);
    }
}
