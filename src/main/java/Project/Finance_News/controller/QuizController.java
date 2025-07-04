package Project.Finance_News.controller;

import Project.Finance_News.dto.QuizDto;
import Project.Finance_News.dto.QuizResultDto;
import Project.Finance_News.dto.QuizSubmitRequest;
import Project.Finance_News.service.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/short-answer")
    public QuizDto getShortAnswerQuiz(@RequestParam Long userId) {
        return quizService.generateShortAnswerQuiz(userId);
    }

    @PostMapping("/submit")
    public QuizResultDto submitQuiz(@RequestBody QuizSubmitRequest request) {
        return quizService.submitQuiz(request.getQuizId(), request.getUserId(), request.getAnswers());    }
}

