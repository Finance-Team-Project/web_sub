package Project.Finance_News.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class    WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/vocabulary/**") // API 경로도 추가
                .excludePathPatterns("/", "/login", "/logout", "/css/**", "/js/**", "/static/**","/api/**",
                                     "/vocabulary/add");
    }
} 