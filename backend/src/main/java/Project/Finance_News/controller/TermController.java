// 위치: Project.Finacne_News.controller.TermController.java

package Project.Finance_News.controller;

import Project.Finance_News.domain.Term;
import Project.Finance_News.repository.TermRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "용어 API")
public class TermController {

    private final TermRepository termRepository;

    @Operation(
            summary = "용어 정보 조회",
            description = "DB에 저장된 금융 용어 중 특정 용어(term)의 설명 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "용어 설명 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 용어가 존재하지 않음")
    })
    @GetMapping("/api/terms/{term}")
    public ResponseEntity<Map<String, Object>> getTermInfo(
            @Parameter(description = "설명을 조회할 금융 용어", required = true)
            @PathVariable String term
    ) {
        Term result = termRepository.findByTerm(term)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Term not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", result.getId());
        response.put("term", result.getTerm());
        response.put("description", result.getDescription());

        return ResponseEntity.ok(response);
    }
}
