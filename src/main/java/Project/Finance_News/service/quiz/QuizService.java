package Project.Finance_News.service.quiz;
import Project.Finance_News.domain.*;
import Project.Finance_News.dto.QuizDto;
import Project.Finance_News.dto.QuizItemDto;
import Project.Finance_News.dto.QuizResultDto;
import Project.Finance_News.repository.*;
import Project.Finance_News.util.KoreanInitialExtractor;
import Project.Finance_News.domain.*;
import Project.Finance_News.dto.QuizDto;
import Project.Finance_News.dto.QuizItemDto;
import Project.Finance_News.dto.QuizResultDto;
import Project.Finance_News.repository.*;
import Project.Finance_News.util.Normalizer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizTermRepository quizTermRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final TermRepository termRepository;



    // 1) 단답형 퀴즈 출제
    @Transactional
    public QuizDto generateShortAnswerQuiz(Long userId) {
        // 1. 퀴즈 생성
        Quiz quiz = new Quiz();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        quiz.setUser(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setType("short_answer");
        quizRepository.save(quiz);

        // 2. userVoca 기반 문제 생성
        List<UserVocabulary> userVocabularies = userVocabularyRepository.findByUserId(userId);
        List<QuizTerm> quizTerms = new ArrayList<>();

        for (UserVocabulary uv : userVocabularies) {
            Term term = uv.getTerm();

            List<Glossary> glossaries = term.getGlossaries();

            if (glossaries.isEmpty()) {
                continue;
            }

            // QuizTerm 생성
            QuizTerm qt = new QuizTerm();
            qt.setQuiz(quiz);
            qt.setTerm(term);
            String initialHint = KoreanInitialExtractor.extractInitials(term.getTerm());
            qt.setInitialHint(initialHint);
            quizTerms.add(qt);
        }

        quizTermRepository.saveAll(quizTerms);
        quiz.setQuizTerms(quizTerms);

        // QuizDto로 변환
        List<QuizItemDto> itemDtos = quizTerms.stream().map(qt -> {
            QuizItemDto item = new QuizItemDto();
            String question = qt.getTerm().getGlossaries().stream()
                    .map(Glossary::getShortDefinition)
                    .collect(Collectors.joining(" / "));
            item.setQuestion(question);
            item.setTermId(qt.getTerm().getId());
            item.setInitialHint(qt.getInitialHint());
            item.setLevel(qt.getTerm().getFrequency() != null ? qt.getTerm().getFrequency() : 1);
            return item;
        }).toList();

        QuizDto dto = new QuizDto();
        dto.setQuizId(quiz.getId());
        dto.setType(quiz.getType());
        dto.setItems(itemDtos);
        return dto;
    }

    // 2) 가로세로 낱말 퀴즈 출제
    @Transactional
    public QuizDto generateCrosswordQuiz(Long userId) {
        System.out.println("[crossword] === 퀴즈 생성 시작 ===");
        // 1. uservoca에서 최근순 5개 단어 선정(중복 없이)
        List<UserVocabulary> userVocabularies = userVocabularyRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("[crossword] uservoca 단어 수: " + userVocabularies.size());
        userVocabularies.forEach(uv -> System.out.println("[crossword] uservoca: " + uv.getTerm().getTerm()));
        if (userVocabularies.size() < 5) {
            System.out.println("[crossword] uservoca 5개 미만, 퀴즈 생성 불가");
            throw new IllegalArgumentException("단어장에 단어를 추가해주세요!");
        }
        List<Term> selectedTerms = userVocabularies.stream()
                .map(UserVocabulary::getTerm)
                .distinct()
                .collect(Collectors.toList());
        System.out.println("[crossword] uservoca 기반 selectedTerms: " + selectedTerms.stream().map(Term::getTerm).collect(Collectors.toList()));
        // 2. term DB에서 추가 단어 선정(기존 단어들과 1글자 이상 겹치는 단어, 중복 없이 최대 10개)
        int assumedFrequency = 1;
        List<Term> allTerms = new ArrayList<>(selectedTerms);
        java.util.Set<Long> usedTermIds = selectedTerms.stream().map(Term::getId).collect(Collectors.toSet());
        int loopCount = 0;
        while (allTerms.size() < 10 && loopCount < 100) {
            int currentSize = allTerms.size();
            List<Term> snapshot = new ArrayList<>(allTerms); // 현재까지의 단어로 baseTerm 후보
            for (Term baseTerm : snapshot) {
                for (char c : baseTerm.getTerm().toCharArray()) {
                    // List<Term> candidates = termRepository.findByCharAndFrequency(c, baseTerm.getFrequency() != null ? baseTerm.getFrequency() : assumedFrequency);
                    List<Term> candidates = termRepository.findByChar(c); // frequency 상관 없이 글자만으로 찾기
                    System.out.println("[crossword] baseTerm: " + baseTerm.getTerm() + ", 글자: " + c + ", 후보: " + candidates.stream().map(Term::getTerm).collect(Collectors.toList()));
                    for (Term candidate : candidates) {
                        if (usedTermIds.contains(candidate.getId())) continue;
                        boolean hasOverlap = false;
                        for (Term t : allTerms) {
                            if (hasCommonChar(candidate.getTerm(), t.getTerm())) {
                                hasOverlap = true;
                                break;
                            }
                        }
                        if (hasOverlap) {
                            allTerms.add(candidate);
                            usedTermIds.add(candidate.getId());
                            System.out.println("[crossword] 추가된 단어: " + candidate.getTerm());
                            if (allTerms.size() >= 10) break;
                        }
                    }
                    if (allTerms.size() >= 10) break;
                }
                if (allTerms.size() >= 10) break;
            }
            if (allTerms.size() == currentSize) {
                System.out.println("[crossword] 더 이상 추가 단어 없음, 반복 종료");
                break;
            }
            loopCount++;
        }
        System.out.println("[crossword] 최종 allTerms: " + allTerms.stream().map(Term::getTerm).collect(Collectors.toList()));
        // 3. 각 단어가 다른 단어와 1글자 이상 겹치는지 최종 체크(불가능하면 5개 이상이면 그대로 반환)
        List<Term> finalTerms = allTerms.stream().distinct().collect(Collectors.toList());
        if (finalTerms.size() < 5) {
            System.out.println("[crossword] 최종 단어 5개 미만, 퀴즈 생성 불가");
            throw new IllegalArgumentException("단어장에 단어를 추가해주세요!");
        }
        // 4. Quiz, QuizTerm 생성
        Quiz quiz = new Quiz();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        quiz.setUser(user);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setType("crossword");
        quizRepository.save(quiz);
        List<QuizTerm> quizTerms = new ArrayList<>();
        for (Term term : finalTerms) {
            if (term.getGlossaries().isEmpty()) {
                System.out.println("[crossword] glossaries 없는 단어(제외): " + term.getTerm());
                continue;
            }
            QuizTerm qt = new QuizTerm();
            qt.setQuiz(quiz);
            qt.setTerm(term);
            String initialHint = KoreanInitialExtractor.extractInitials(term.getTerm());
            qt.setInitialHint(initialHint);
            quizTerms.add(qt);
        }
        quizTermRepository.saveAll(quizTerms);
        quiz.setQuizTerms(quizTerms);
        System.out.println("[crossword] 최종 퀴즈에 포함된 단어: " + quizTerms.stream().map(qt -> qt.getTerm().getTerm()).collect(Collectors.toList()));
        // 5. QuizDto로 변환
        // --- 실제 가능한 크로스워드 퍼즐 배치 알고리즘 ---
        class PlacedWord {
            Term term;
            int row, col;
            String direction;
            int number;
            int overlapIdx; // 겹친 글자 인덱스(자신 기준)
            int baseRow, baseCol; // 겹친 위치(보드 기준)
            PlacedWord(Term term, int row, int col, String direction, int number, int overlapIdx, int baseRow, int baseCol) {
                this.term = term; this.row = row; this.col = col; this.direction = direction; this.number = number;
                this.overlapIdx = overlapIdx; this.baseRow = baseRow; this.baseCol = baseCol;
            }
        }
        int width = 15, height = 15;
        char[][] board = new char[height][width];
        for (char[] row : board) java.util.Arrays.fill(row, '.');
        List<PlacedWord> placed = new ArrayList<>();
        int centerR = height/2, centerC = width/2;
        int number = 1;
        // 1. 첫 단어: 중앙 가로
        if (!quizTerms.isEmpty()) {
            Term first = quizTerms.get(0).getTerm();
            int len = first.getTerm().length();
            int startC = centerC - len/2;
            for (int i = 0; i < len; i++) board[centerR][startC+i] = first.getTerm().charAt(i);
            placed.add(new PlacedWord(first, centerR, startC, "across", number++, -1, -1, -1));
        }
        // 2. 나머지 단어들 배치
        outer: for (int qi = 1; qi < quizTerms.size(); qi++) {
            Term t = quizTerms.get(qi).getTerm();
            String word = t.getTerm();
            boolean placedFlag = false;
            // 이미 배치된 단어들과 겹침 탐색
            for (PlacedWord pw : placed) {
                String base = pw.term.getTerm();
                for (int i = 0; i < word.length(); i++) {
                    char ch = word.charAt(i);
                    for (int j = 0; j < base.length(); j++) {
                        if (ch != base.charAt(j)) continue;
                        // 배치 시도: base가 가로면 세로, 세로면 가로
                        String dir = pw.direction.equals("across") ? "down" : "across";
                        int r = pw.row, c = pw.col;
                        if (pw.direction.equals("across")) {
                            // base: 가로, word: 세로
                            int startR = r - i;
                            int startC = c + j;
                            if (startR < 0 || startR + word.length() > height) continue;
                            boolean conflict = false;
                            for (int k = 0; k < word.length(); k++) {
                                char cell = board[startR+k][startC];
                                if (cell != '.' && cell != word.charAt(k)) { conflict = true; break; }
                                // 교차점 이외에 이미 같은 방향 단어가 있으면 안 됨
                                if (cell != '.' && k != i) {
                                    // 이미 배치된 단어와 교차점 외 겹침 방지
                                    for (PlacedWord other : placed) {
                                        if (other.direction.equals("down") && other.col == startC && other.row <= startR+k && startR+k < other.row+other.term.getTerm().length()) {
                                            conflict = true; break;
                                        }
                                    }
                                }
                            }
                            if (conflict) continue;
                            // 배치
                            for (int k = 0; k < word.length(); k++) board[startR+k][startC] = word.charAt(k);
                            placed.add(new PlacedWord(t, startR, startC, dir, number++, i, r, c+j));
                            placedFlag = true;
                            break outer;
                        } else {
                            // base: 세로, word: 가로
                            int startR = r + j;
                            int startC = c - i;
                            if (startC < 0 || startC + word.length() > width) continue;
                            boolean conflict = false;
                            for (int k = 0; k < word.length(); k++) {
                                char cell = board[startR][startC+k];
                                if (cell != '.' && cell != word.charAt(k)) { conflict = true; break; }
                                if (cell != '.' && k != i) {
                                    for (PlacedWord other : placed) {
                                        if (other.direction.equals("across") && other.row == startR && other.col <= startC+k && startC+k < other.col+other.term.getTerm().length()) {
                                            conflict = true; break;
                                        }
                                    }
                                }
                            }
                            if (conflict) continue;
                            for (int k = 0; k < word.length(); k++) board[startR][startC+k] = word.charAt(k);
                            placed.add(new PlacedWord(t, startR, startC, dir, number++, i, startR, startC+i));
                            placedFlag = true;
                            break outer;
                        }
                    }
                }
            }
            // 배치 실패 시 스킵
        }
        // 3. QuizItemDto로 변환(배치된 단어만)
        List<QuizItemDto> itemDtos = new ArrayList<>();
        for (PlacedWord pw : placed) {
            QuizItemDto item = new QuizItemDto();
            String question = pw.term.getGlossaries().stream()
                    .map(Glossary::getShortDefinition)
                    .collect(Collectors.joining(" / "));
            item.setQuestion(question);
            item.setTermId(pw.term.getId());
            item.setInitialHint(KoreanInitialExtractor.extractInitials(pw.term.getTerm()));
            item.setLevel(pw.term.getFrequency() != null ? pw.term.getFrequency() : 1);
            item.setTerm(pw.term.getTerm());
            item.setClue(question);
            item.setNumber(pw.number);
            item.setLength(pw.term.getTerm().length());
            item.setRow(pw.row);
            item.setCol(pw.col);
            item.setDirection(pw.direction);
            itemDtos.add(item);
        }
        QuizDto dto = new QuizDto();
        dto.setQuizId(quiz.getId());
        dto.setType(quiz.getType());
        dto.setItems(itemDtos);
        System.out.println("[crossword] 배치 결과: " + itemDtos.stream().map(i -> i.getTerm() + ":(" + i.getRow() + "," + i.getCol() + "," + i.getDirection() + ")").collect(Collectors.toList()));
        System.out.println("[crossword] === 퀴즈 생성 종료 ===");
        return dto;
    }

    // 단어 간 공통 문자 1개 이상 있는지 검사
    private boolean hasCommonChar(String a, String b) {
        for (char c : a.toCharArray()) {
            if (b.indexOf(c) >= 0) return true;
        }
        return false;
    }
    // 2) 사용자 퀴즈 응답을 채점 & 결과 저장
    @Transactional
    public QuizResultDto submitQuiz(Long quizId, Long userId, Map<Long, String> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        int score = 0;
        Map<Long, Boolean> correctMap = new HashMap<>();


        for (QuizTerm qt : quiz.getQuizTerms()) {
            Long termId = qt.getTerm().getId();
            String userAnswer = answers.get(termId); // 이건 quiz_term의 ID임
            String correctAnswer = qt.getTerm().getTerm();

            String normalizedUser = Normalizer.normalize(userAnswer);
            String normalizedCorrect = Normalizer.normalize(correctAnswer);

            boolean correct = userAnswer != null && normalizedUser.equals(normalizedCorrect);

            if (correct) score += 10;
            correctMap.put(termId, correct); // 여기 주의! qt.getId()가 아니라 termId
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
                result.getTakenAt(),
                correctMap
        );
    }


}