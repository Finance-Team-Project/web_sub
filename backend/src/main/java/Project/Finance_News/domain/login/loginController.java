package Project.Finance_News.domain.login;

import Project.Finance_News.domain.User;
import Project.Finance_News.domain.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class loginController {

    private final loginService loginService;

    @GetMapping("/login")
    public String loginForm(){
        // 전용 로그인 페이지 대신 홈페이지로 리다이렉트
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm")loginForm form, BindingResult result,
                        HttpServletRequest request, Model model){
        if(result.hasErrors()){
            // 에러 발생 시 홈페이지로 리다이렉트 (모델에 에러 정보 포함)
            model.addAttribute("loginForm", form);
            return "home";
        }

        User loginUser = loginService.login(form.getLoginId(), form.getPassword());

        if(loginUser == null){
            result.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            model.addAttribute("loginForm", form);
            return "home";
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
