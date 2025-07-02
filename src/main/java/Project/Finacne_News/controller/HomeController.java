package Project.Finacne_News.controller;

import Project.Finacne_News.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(
            @SessionAttribute(name = "loginUser", required = false) User loginUser,
            Model model) {

        if(loginUser == null){
            return "home";  // templates/home.html 렌더링됨
        }

        model.addAttribute("user", loginUser);
        return "loginHome";
    }
}
