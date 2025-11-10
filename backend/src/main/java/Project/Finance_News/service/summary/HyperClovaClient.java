package Project.Finance_News.service.summary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * HyperCLOVA Summarization API 연동을 위한 클라이언트 클래스
 */
@Service
@Component
public class HyperClovaClient {

    private WebClient webClient = WebClient.builder()
            .baseUrl("https://clovastudio.stream.ntruss.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private String apiKey;

    @Value("${clova.api.key}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSummary(String promptText) {
        // 요청 바디 구성
        Map<String, Object> requestBody = Map.of(
                "texts", List.of(promptText),
                "autoSentenceSplitter", true,
                "segCount", -1,
                "segMaxSize", 1000,
                "segMinSize", 300,
                "includeAiFilters", false
        );

        try {
            return webClient.post()
                    .uri("/testapp/v1/api-tools/summarization/v2")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        System.out.println("📦 [DEBUG] 전체 응답: " + response);
                    
                        Object result = response.get("result");
                        if (result instanceof Map<?, ?> resultMap) {
                            Object text = resultMap.get("text");
                            if (text != null) {
                                System.out.println("✅ [요약 결과]: " + text);
                                return text.toString();
                            }
                        }
                    
                        return "요약 결과가 없습니다.";
                    })
                    .onErrorReturn("요약 중 오류가 발생했습니다.")
                    .block();
        } catch (Exception e) {
            return "요약 요청 실패: " + e.getMessage();
        }
    }

    public String getChatCompletion(List<Map<String, String>> messages) {
        // CLOVA Studio Chat Completions API 엔드포인트
        String model = "HCX-DASH-001"; // 실제 사용 모델명으로 교체 필요
        String apiUrl = "/testapp/v1/chat-completions/" + model;

        Map<String, Object> requestBody = Map.of(
                "messages", messages,
                "topP", 0.8,
                "topK", 0,
                "maxTokens", 1024,
                "temperature", 0.7
        );

        try {
            return webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        System.out.println("📦 [DEBUG] 전체 응답: " + response);
                        Object result = response.get("result");
                        if (result instanceof Map<?, ?> resultMap) {
                            Object messageObj = resultMap.get("message");
                            if (messageObj instanceof Map<?, ?> messageMap) {
                                Object content = messageMap.get("content");
                                if (content != null) {
                                    System.out.println("✅ [챗봇 요약 결과]: " + content);
                                    return content.toString();
                                }
                            }
                        }
                        return "챗봇 응답이 없습니다.";
                    })
                    .onErrorReturn("챗봇 호출 중 오류가 발생했습니다.")
                    .block();
        } catch (Exception e) {
            return "챗봇 요청 실패: " + e.getMessage();
        }
    }
}