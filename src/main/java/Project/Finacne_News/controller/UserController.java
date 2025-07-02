package Project.Finacne_News.controller;

import Project.Finacne_News.domain.User;
import Project.Finacne_News.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/users/add")
    public String addForm(@ModelAttribute("user") User user) {
        return "users/addUserForm";
    }

    @PostMapping("/users/add")
    public String save(@Valid @ModelAttribute("user") User user, BindingResult result) {
        if(result.hasErrors()){
            return "users/addUserForm";
        }

        userRepository.save(user);
        return "login/loginForm";
    }
}
