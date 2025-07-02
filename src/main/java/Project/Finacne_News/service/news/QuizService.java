package Project.Finacne_News.service.news;
import Project.Finacne_News.domain.*;
import Project.Finacne_News.dto.QuizDto;
import Project.Finacne_News.dto.QuizItemDto;
import Project.Finacne_News.dto.QuizResultDto;
import Project.Finacne_News.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizTermRepository quizTermRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;


    // 1) 단답형 퀴즈 출제
    @Transactional
    public QuizDto generateShortAnswerQuiz(Long userId) {
        // 1. 퀴즈 생성
        Quiz quiz = new Quiz();
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setType("short_answer");
        quizRepository.save(quiz);

        // 2. userVoca 기반 문제 생성
        List<UserVocabulary> userVocabularies = userVocabularyRepository.findByUserId(userId);
        List<QuizTerm> quizTerms = new ArrayList<>();

        for (UserVocabulary uv : userVocabularies) {
            Term term = uv.getTerm();

            // 3. QuizTerm 생성
            QuizTerm qt = new QuizTerm();
            qt.setQuiz(quiz);
            qt.setTerm(term);
            quizTerms.add(qt);
        }

        quizTermRepository.saveAll(quizTerms);
        quiz.setQuizTerms(quizTerms);

        // 3. QuizDto로 변환
        List<QuizItemDto> itemDtos = quizTerms.stream().map(qt -> {
            QuizItemDto item = new QuizItemDto();
            String question = qt.getTerm().getGlossaries().stream()
                    .map(Glossary::getShortDefinition)
                    .collect(Collectors.joining(" / "));
            item.setQuestion(question);
            item.setTermId(qt.getTerm().getId());
            return item;
        }).toList();

        QuizDto dto = new QuizDto();
        dto.setQuizId(quiz.getId());
        dto.setType(quiz.getType());
        dto.setItems(itemDtos);
        return dto;
    }


    // 2) 가로세로 낱말 퀴즈 출제

    // 3) 사용자 퀴즈 응답을 채점 & 결과 저장
    @Transactional
    public QuizResultDto submitQuiz(Long quizId, Long userId, Map<Long, String> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        int score = 0;

        for (QuizTerm qt : quiz.getQuizTerms()) {
            String userAnswer = answers.get(qt.getId());
            String correctAnswer = qt.getTerm().getTerm();

            if (userAnswer != null && userAnswer.equalsIgnoreCase(correctAnswer)) {
                score+=10;
            }
        }

        // 결과 저장
        QuizResult result = new QuizResult();
        result.setQuiz(quiz);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        result.setUser(user);

        result.setScore(score);
        result.setTakenAt(LocalDateTime.now());
        quizResultRepository.save(result);

        return new QuizResultDto(
                quiz.getId(),
                user.getId(),
                score,
                result.getTakenAt()
        );
    }
}