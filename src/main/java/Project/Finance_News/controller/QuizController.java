package Project.Finance_News.controller;

import Project.Finance_News.dto.QuizDto;
import Project.Finance_News.dto.QuizResultDto;
import Project.Finance_News.dto.QuizSubmitRequest;
import Project.Finance_News.service.quiz.QuizService;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.session.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/short-answer")
    public QuizDto getShortAnswerQuiz(HttpSession session) {
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        return quizService.generateShortAnswerQuiz(user.getId());
    }

    @GetMapping("/crossword")
    public QuizDto getCrosswordQuiz(HttpSession session) {
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        return quizService.generateCrosswordQuiz(user.getId());
    }

    @PostMapping("/submit")
    public QuizResultDto submitQuiz(@RequestBody QuizSubmitRequest request, HttpSession session) {
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        return quizService.submitQuiz(request.getQuizId(), user.getId(), request.getAnswers());
    }

    @PostMapping("/submit-crossword")
    public Project.Finance_News.dto.CrosswordResultDto submitCrossword(@RequestBody Project.Finance_News.dto.CrosswordSubmitRequest request, HttpSession session) {
        User user = (User) session.getAttribute(SessionConst.LOGIN_USER);
        return quizService.submitCrossword(user.getId(), request.getAnswers());
    }
}



