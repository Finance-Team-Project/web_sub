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
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizTermRepository quizTermRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final TermRepository termRepository;

    private static class PlacedWord {
        Term term;
        int row, col;
        String direction;
        int number;
        int overlapIdx;
        int baseRow, baseCol;
        PlacedWord(Term term, int row, int col, String direction, int number, int overlapIdx, int baseRow, int baseCol) {
            this.term = term; this.row = row; this.col = col; this.direction = direction; this.number = number;
            this.overlapIdx = overlapIdx; this.baseRow = baseRow; this.baseCol = baseCol;
        }
    }


    // 1) 단답형 퀴즈 출제
    @Transactional
    public QuizDto generateShortAnswerQuiz(Long userId) {
        // 1. 퀴즈 생성
        Quiz quiz = new Quiz();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        quiz.setUser(user);
        quiz.setCreatedAt(LocalDateTime.now());        quiz.setType("short_answer");

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
            String answer = qt.getTerm().getTerm();
            List<Glossary> glossaries = qt.getTerm().getGlossaries();
            StringBuilder questionBuilder = new StringBuilder();
            int glossCount = Math.min(3, glossaries.size());
            if (glossCount == 1) {
                // 번호 없이 그대로, 정답 단어는 네모로 치환
                String def = glossaries.get(0).getShortDefinition();
                String replaced = def.replaceAll(answer, "□".repeat(answer.length()));
                questionBuilder.append(replaced);
            } else {
                for (int i = 0; i < glossCount; i++) {
                    String def = glossaries.get(i).getShortDefinition();
                    String replaced = def.replaceAll(answer, "□".repeat(answer.length()));
                    questionBuilder.append("(").append(i+1).append(") ").append(replaced);
                    if (i < glossCount-1) questionBuilder.append("\n");
                }
            }
            item.setQuestion(questionBuilder.toString());
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
        // 1. uservoca에서 가장 최근 단어 1개 추출
        UserVocabulary userVoca = userVocabularyRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (userVoca == null) throw new IllegalArgumentException("단어장에 단어를 추가해주세요!");
        Term first = userVoca.getTerm();

        // 2. 후보군 준비 (term 테이블에서 glossaries 있는 단어만)
        List<Term> allTerms = termRepository.findAll().stream()
            .filter(t -> t.getGlossaries() != null && !t.getGlossaries().isEmpty())
            .collect(Collectors.toList());

        Set<Long> usedTermIds = new HashSet<>();
        usedTermIds.add(first.getId());

        // 3. 퍼즐 보드 준비
        int width = 15, height = 15;
        char[][] board = new char[height][width];
        for (char[] row : board) Arrays.fill(row, '.');

        class PlacedWord {
            Term term; int row, col, length, number; String direction;
            PlacedWord(Term term, int row, int col, String direction, int number) {
                this.term = term; this.row = row; this.col = col; this.length = term.getTerm().length();
                this.direction = direction; this.number = number;
            }
        }
        List<PlacedWord> placed = new ArrayList<>();
        int number = 1;

        // 4. 첫 단어: 가로 중앙
        int firstLen = first.getTerm().length();
        int centerR = height/2, centerC = width/2 - firstLen/2;
        for (int i = 0; i < firstLen; i++) board[centerR][centerC+i] = first.getTerm().charAt(i);
        placed.add(new PlacedWord(first, centerR, centerC, "across", number++));
        List<Term> quizTerms = new ArrayList<>();
        quizTerms.add(first);

        // 5. 연결 가능한 단어 계속 추가
        Random rand = new Random();
        while (placed.size() < 10) {
            boolean added = false;
            // (1) 연결 가능한 후보 찾기
            List<Term> candidates = allTerms.stream()
                .filter(t -> !usedTermIds.contains(t.getId()))
                .collect(Collectors.toList());
            for (Term t : candidates) {
                String word = t.getTerm();
                boolean found = false;
                // (2) 이미 배치된 단어들과 교차점 찾기
                for (PlacedWord pw : placed) {
                    String base = pw.term.getTerm();
                    for (int i = 0; i < word.length(); i++) {
                        char ch = word.charAt(i);
                        for (int j = 0; j < base.length(); j++) {
                            if (ch != base.charAt(j)) continue;
                            String dir = pw.direction.equals("across") ? "down" : "across";
                            int len = word.length();
                            int tryRow, tryCol;
                            if (dir.equals("across")) {
                                tryRow = pw.row - i;
                                tryCol = pw.col + j;
                                if (tryRow < 0 || tryRow >= height || tryCol < 0 || tryCol + len > width) continue;
                                boolean conflict = false;
                                for (int k = 0; k < len; k++) {
                                    char cell = board[tryRow][tryCol + k];
                                    if (cell != '.' && cell != word.charAt(k)) { conflict = true; break; }
                                }
                                if (conflict) continue;
                                for (int k = 0; k < len; k++) board[tryRow][tryCol + k] = word.charAt(k);
                                placed.add(new PlacedWord(t, tryRow, tryCol, "across", number++));
                                usedTermIds.add(t.getId());
                                quizTerms.add(t);
                                found = true; added = true;
                                break;
                            } else {
                                tryRow = pw.row + j;
                                tryCol = pw.col - i;
                                if (tryCol < 0 || tryCol >= width || tryRow < 0 || tryRow + len > height) continue;
                                boolean conflict = false;
                                for (int k = 0; k < len; k++) {
                                    char cell = board[tryRow + k][tryCol];
                                    if (cell != '.' && cell != word.charAt(k)) { conflict = true; break; }
                                }
                                if (conflict) continue;
                                for (int k = 0; k < len; k++) board[tryRow + k][tryCol] = word.charAt(k);
                                placed.add(new PlacedWord(t, tryRow, tryCol, "down", number++));
                                usedTermIds.add(t.getId());
                                quizTerms.add(t);
                                found = true; added = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                    if (found) break;
                }
                if (added) break;
            }
            // (3) 연결 불가하면 빈 공간에 새 단어 배치(가로/세로 랜덤)
            if (!added && !candidates.isEmpty()) {
                Term t = candidates.get(rand.nextInt(candidates.size()));
                String word = t.getTerm();
                int len = word.length();
                boolean placedFlag = false;
                // 가로
                outer: for (int r = 0; r < height; r++) {
                    for (int c = 0; c <= width - len; c++) {
                        boolean ok = true;
                        for (int k = 0; k < len; k++) {
                            if (board[r][c+k] != '.') { ok = false; break; }
                        }
                        if (ok) {
                            for (int k = 0; k < len; k++) board[r][c+k] = word.charAt(k);
                            placed.add(new PlacedWord(t, r, c, "across", number++));
                            usedTermIds.add(t.getId());
                            quizTerms.add(t);
                            placedFlag = true;
                            break outer;
                        }
                    }
                }
                // 세로
                if (!placedFlag) {
                    outer2: for (int c = 0; c < width; c++) {
                        for (int r = 0; r <= height - len; r++) {
                            boolean ok = true;
                            for (int k = 0; k < len; k++) {
                                if (board[r+k][c] != '.') { ok = false; break; }
                            }
                            if (ok) {
                                for (int k = 0; k < len; k++) board[r+k][c] = word.charAt(k);
                                placed.add(new PlacedWord(t, r, c, "down", number++));
                                usedTermIds.add(t.getId());
                                quizTerms.add(t);
                                break outer2;
                            }
                        }
                    }
                }
            }
            if (!added && candidates.isEmpty()) break; // 더 이상 추가 불가
        }

        // 6. 결과 변환
        List<CrosswordItem> result = new ArrayList<>();
        for (PlacedWord pw : placed) {
            String clue = pw.term.getGlossaries().get(0).getShortDefinition();
            result.add(new CrosswordItem(
                pw.row, pw.col, pw.length, pw.direction, pw.term.getTerm(), clue, pw.number
            ));
        }
        List<QuizItemDto> quizItems = result.stream().map(item -> {
            QuizItemDto dto = new QuizItemDto();
            dto.setRow(item.row);
            dto.setCol(item.col);
            dto.setLength(item.length);
            dto.setDirection(item.direction);
            dto.setTerm(item.term);
            dto.setQuestion(item.clue);
            dto.setNumber(item.number);
            return dto;
        }).collect(Collectors.toList());

        QuizDto quizDto = new QuizDto();
        quizDto.setType("crossword");
        quizDto.setItems(quizItems);
        return quizDto;
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