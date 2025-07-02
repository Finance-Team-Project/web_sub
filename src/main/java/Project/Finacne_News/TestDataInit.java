package Project.Finacne_News;

import Project.Finacne_News.domain.User;
import Project.Finacne_News.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final UserRepository userRepository;

    /**
     * 테스트용 데이터 추가
     * */
    @PostConstruct
    public void init(){
        User user1 = new User();

        user1.setLoginId("1234");
        user1.setPassword("1234");
        user1.setNickname("시스");
        user1.setName("김준원");
        user1.setJob("학생");
        user1.setGoal("경제 지식이 부족해 이 부분을 함양하기 위한 목적이 있습니다.");

        User user2 = new User();
        user2.setLoginId("1111");
        user2.setPassword("1111");
        user2.setNickname("억까시치");
        user2.setName("박세영");
        user2.setJob("자영업자");
        user2.setGoal("자영업을 하고 있는 사업자 인데, 이 뉴스가 경제에 어떤 영향을 끼칠지 분석하기 위한 목적이 있습니다.");

        userRepository.save(user1);
        userRepository.save(user2);
    }
}
