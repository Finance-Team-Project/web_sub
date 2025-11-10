package Project.Finance_News.config;

import Project.Finance_News.domain.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object user = (session != null) ? session.getAttribute(SessionConst.LOGIN_USER) : null;
        if (user == null) {
            response.sendRedirect("/login"); // 또는 response.setStatus(401);
            return false;
        }
        return true;
    }
} 