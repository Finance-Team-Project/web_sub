package Project.Finance_News.domain.login;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class loginForm {
    @NotEmpty(message = "아이디는 필수입니다.")
    private String loginId;
    @NotEmpty(message = "비밀번호는 필수입니다.")
    private String password;
}
