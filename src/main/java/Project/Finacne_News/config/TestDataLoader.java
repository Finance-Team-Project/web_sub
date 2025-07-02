package Project.Finacne_News.config;

import Project.Finacne_News.domain.*;
import Project.Finacne_News.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {


    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final GlossaryRepository glossaryRepository;
    private final UserVocabularyRepository userVocabularyRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("tester@quiz.com").isPresent()) return;

        // 1. 테스트 사용자 생성
        User user = new User();
        user.setEmail("tester@quiz.com");
        user.setPasswordHash("test1234"); // 실환경에서는 암호화 필요
        user.setNickname("QuizTester");
        user.setRole("USER");
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);

        // 2. Term + 다수의 Glossary + UserVocabulary
        createTermWithMultipleGlossaries("블록체인", user,
                List.of("분산된 장부 기술",
                        "중앙기관 없이 거래 기록 유지",
                        "비트코인의 핵심 기술"));

        createTermWithMultipleGlossaries("핀테크", user,
                List.of("기술 기반 금융 서비스",
                        "모바일 결제와 인터넷 뱅킹 포함",
                        "금융과 IT 융합"));

        createTermWithMultipleGlossaries("토큰화", user,
                List.of("실물 자산을 디지털 토큰으로 표현",
                        "블록체인 기반 자산 표현 기법",
                        "부동산이나 예술품 등 실물에 적용 가능"));
    }

    private void createTermWithMultipleGlossaries(String termStr, User user, List<String> glossaryDefs) {
        // Term 생성
        Term term = new Term();
        term.setTerm(termStr);
        term.setDescription(glossaryDefs.get(0));  // 대표 설명
        term.setLastUpdated(LocalDateTime.now());
        termRepository.save(term);

        // Glossary 여러 개 생성
        for (String def : glossaryDefs) {
            Glossary glossary = new Glossary();
            glossary.setTerm(term);
            glossary.setShortDefinition(def);
            glossary.setFullExplanation(def + "에 대한 자세한 설명입니다.");
            glossaryRepository.save(glossary);
        }

        // UserVocabulary 생성
        UserVocabulary uv = new UserVocabulary();
        uv.setUser(user);
        uv.setTerm(term);
        userVocabularyRepository.save(uv);
    }
}
