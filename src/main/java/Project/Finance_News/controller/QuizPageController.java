package Project.Finance_News.controller;

import jakarta.servlet.http.HttpSession;
import Project.Finance_News.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class QuizPageController {

    @GetMapping("/quiz/short-answer")
    public String showShortAnswerQuizPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            System.out.println("🚨 세션에 로그인된 유저 없음");  // ← 로그 추가
            return "redirect:/login";
        }

        System.out.println("✅ 로그인 유저: " + user.getId() + ", " + user.getNickname());


        model.addAttribute("userId", user.getId());
        model.addAttribute("userNickname", user.getNickname());

        return "quiz/quiz"; // templates/quiz.html
    }

    @GetMapping("/quiz/crossword")
    public String showCrosswordQuizPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            System.out.println("🚨 세션에 로그인된 유저 없음");
            return "redirect:/login";
        }
        System.out.println("✅ 로그인 유저: " + user.getId() + ", " + user.getNickname());
        model.addAttribute("userId", user.getId());
        model.addAttribute("userNickname", user.getNickname());
        return "quiz/crossword"; // templates/quiz/crossword.html
    }
}
