package Project.Finance_News.controller;

import Project.Finance_News.dto.SummaryRequestDto;
import Project.Finance_News.service.summary.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 뉴스 요약 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summarize")
@Tag(name = "뉴스 요약 API", description = "뉴스 본문을 사용자의 성향에 맞게 요약하는 기능을 제공합니다.")
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping
    @Operation(
            summary = "뉴스 요약 요청",
            description = "뉴스 ID와 사용자 ID를 받아 사용자의 특성에 맞는 요약 결과를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요약 성공"),
            @ApiResponse(responseCode = "400", description = "요약 요청 데이터 오류"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 또는 AI 요약 실패")
    })
    public ResponseEntity<String> summarize(@RequestBody SummaryRequestDto request) {
        String summary = summaryService.summarize(request.getNewsId(), request.getUserId());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/chat")
    @Operation(
            summary = "맞춤형 뉴스 요약(chat completions)",
            description = "뉴스 ID와 사용자 ID를 받아 chat completions 기반 맞춤형 요약 결과를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "챗봇 요약 성공"),
            @ApiResponse(responseCode = "400", description = "요약 요청 데이터 오류"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 또는 AI 요약 실패")
    })
    public ResponseEntity<String> summarizeWithChat(@RequestBody SummaryRequestDto request) {
        String summary = summaryService.summarizeWithChat(request.getNewsId(), request.getUserId());
        return ResponseEntity.ok(summary);
    }
}
