package Project.Finance_News.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.session.SessionConst;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
            if (loginUser != null) {
                model.addAttribute("user", loginUser);
                return "loginHome";
            }
        }
        return "home";
    }
}
