package Project.Finacne_News.domain.login;

import Project.Finacne_News.domain.User;
import Project.Finacne_News.domain.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Member;

@Controller
@RequiredArgsConstructor
public class loginController {

    private final loginService loginService;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm")loginForm form){
        return "login/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm")loginForm form, BindingResult result,
                        HttpServletRequest request){
        if(result.hasErrors()){
            return "login/loginForm";
        }

        User loginUser = loginService.login(form.getLoginId(), form.getPassword());

        if(loginUser == null){
            result.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        HttpSession session = request.getSession();

        session.setAttribute(SessionConst.LOGIN_USER, loginUser);

        return "redirect:/";
    }


    @PostMapping("logout")
    public String logout(HttpServletRequest request){
        // 세션을 삭제한다.
        HttpSession session = request.getSession(false);
        if(session != null){
            session.invalidate();
        }
        return "redirect:/";
    }
}
