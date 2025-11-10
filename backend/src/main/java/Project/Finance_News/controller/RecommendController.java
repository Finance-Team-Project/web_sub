package Project.Finance_News.controller;

import Project.Finance_News.domain.User;
import Project.Finance_News.domain.session.SessionConst;
import Project.Finance_News.dto.RecommendResponseDto;
import Project.Finance_News.service.news.FastApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecommendController {
    private final FastApiService fastApiService;
    
    @GetMapping("/news/recommendations")
    public ResponseEntity<RecommendResponseDto> getRecommendations(
            @SessionAttribute(name = SessionConst.LOGIN_USER) User loginUser) {
        try {
            RecommendResponseDto recommendations = 
                fastApiService.getRecommendations(loginUser);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("추천 뉴스 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
