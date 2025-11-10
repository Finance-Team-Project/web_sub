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
import Project.Finance_News.domain.UserPoint;
import Project.Finance_News.util.Normalizer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import Project.Finance_News.service.badge.BadgeGrantService;
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
import java.util.Queue;
import java.util.ArrayDeque;
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
    private final UserPointRepository userPointRepository;
    private final BadgeGrantService badgeGrantService;

    private static class PlacedWord {
        Term term; int row, col, length, number; String direction;
        int overlapIdx, baseRow, baseCol;
        String overlapWith; // 겹치는 단어 정보 (예: "금리-금")
        
        PlacedWord(Term term, int row, int col, String direction, int number, int overlapIdx, int baseRow, int baseCol) {
            this.term = term; this.row = row; this.col = col; this.length = term.getTerm().length();
            this.direction = direction; this.number = number;
            this.overlapIdx = overlapIdx; this.baseRow = baseRow; this.baseCol = baseCol;
        }
        
        PlacedWord(Term term, int row, int col, String direction, int number, int overlapIdx, int baseRow, int baseCol, String overlapWith) {
            this.term = term; this.row = row; this.col = col; this.length = term.getTerm().length();
            this.direction = direction; this.number = number;
            this.overlapIdx = overlapIdx; this.baseRow = baseRow; this.baseCol = baseCol;
            this.overlapWith = overlapWith;
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
            // 정답 보기 기능을 위해 정답 단어 포함 (클라이언트에서 제출 후 오답만 노출)
            item.setTerm(answer);
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
        // 1. 최근 uservoca 5개 고정
        List<UserVocabulary> userVocabs = userVocabularyRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        if (userVocabs.isEmpty()) throw new IllegalArgumentException("단어장에 단어를 추가해주세요!");

        List<Term> userTerms = userVocabs.stream().map(UserVocabulary::getTerm).toList();
        Set<Long> userTermIds = userTerms.stream().map(Term::getId).collect(Collectors.toSet());

        // 2. term 전체에서 각 uservoca 단어와 1글자 이상 겹치는 단어들을 서로 다른 글자별로 분류하여 후보군 구성
        List<Term> allTerms = termRepository.findAll().stream()
            .filter(t -> t.getGlossaries() != null && !t.getGlossaries().isEmpty())
            .filter(t -> !userTermIds.contains(t.getId()))
            .collect(Collectors.toList());
        
        List<List<Term>> verticalTermsList = new ArrayList<>();
        for (Term userTerm : userTerms) {
            String userWord = userTerm.getTerm();
            List<Term> selectedVerticals = new ArrayList<>();
            Set<Long> usedTermIds = new HashSet<>();
            
            // 글자 수에 따른 후보군 설정
            if (userWord.length() >= 3) {
                // 3글자 이상: 1번째 글자와 겹치는 단어 1개, 3번째 글자와 겹치는 단어 1개
                char firstChar = userWord.charAt(0);
                char thirdChar = userWord.charAt(2);
                
                // 1번째 글자와 겹치는 단어 찾기
                for (Term t : allTerms) {
                    if (!usedTermIds.contains(t.getId()) && t.getTerm().indexOf(firstChar) >= 0) {
                        selectedVerticals.add(t);
                        usedTermIds.add(t.getId());
                        break;
                    }
                }
                
                // 3번째 글자와 겹치는 단어 찾기
                for (Term t : allTerms) {
                    if (!usedTermIds.contains(t.getId()) && t.getTerm().indexOf(thirdChar) >= 0) {
                        selectedVerticals.add(t);
                        usedTermIds.add(t.getId());
                        break;
                    }
                }
            } else {
                // 3글자 미만: 두 글자 단어도 후보군은 1개만 선택 (첫 번째 글자 기준)
                char firstChar = userWord.charAt(0);

                // 첫 번째 글자와 겹치는 단어 1개만 선택
                for (Term t : allTerms) {
                    if (!usedTermIds.contains(t.getId()) && t.getTerm().indexOf(firstChar) >= 0) {
                        selectedVerticals.add(t);
                        usedTermIds.add(t.getId());
                        break;
                    }
                }
            }
            
            // 디버깅 출력
            System.out.println("[" + userWord + "] 후보군 구성:");
            if (userWord.length() >= 3) {
                System.out.println("  1번째 글자('" + userWord.charAt(0) + "')와 겹치는 단어: " + 
                    (selectedVerticals.size() > 0 ? selectedVerticals.get(0).getTerm() : "없음"));
                System.out.println("  3번째 글자('" + userWord.charAt(2) + "')와 겹치는 단어: " + 
                    (selectedVerticals.size() > 1 ? selectedVerticals.get(1).getTerm() : "없음"));
            } else {
                System.out.println("  1번째 글자('" + userWord.charAt(0) + "')와 겹치는 단어: " + 
                    (selectedVerticals.size() > 0 ? selectedVerticals.get(0).getTerm() : "없음"));
                if (userWord.length() >= 2) {
                    System.out.println("  2번째 글자('" + userWord.charAt(1) + "')와 겹치는 단어: " + 
                        (selectedVerticals.size() > 1 ? selectedVerticals.get(1).getTerm() : "없음"));
                }
            }
            
            verticalTermsList.add(selectedVerticals);
        }

        // 후보군 총 15개 출력
        System.out.println("=== 후보군 총 15개 ===");
        int totalCandidates = 0;
        for (int setIdx = 0; setIdx < userTerms.size(); setIdx++) {
            Term hori = userTerms.get(setIdx);
            List<Term> verticals = verticalTermsList.get(setIdx);
            System.out.print("[" + hori.getTerm() + "]와 겹치는 후보: ");
            for (Term v : verticals) {
                System.out.print(v.getTerm() + ", ");
                totalCandidates++;
            }
            System.out.println();
            System.out.println("[" + hori.getTerm() + "] 후보군 개수: " + verticals.size());
            
            // 금리 단어에 대해 더 자세한 정보 출력
            if (hori.getTerm().equals("금리")) {
                System.out.println("=== 금리 디버깅 ===");
                System.out.println("금리 단어 길이: " + hori.getTerm().length());
                System.out.println("금리 글자들: " + hori.getTerm().charAt(0) + ", " + hori.getTerm().charAt(1));
                System.out.println("allTerms에서 금리와 겹치는 모든 단어:");
                for (Term t : allTerms) {
                    if (hasCommonChar(hori.getTerm(), t.getTerm())) {
                        System.out.println("  - " + t.getTerm() + " (겹치는 글자: " + getCommonChars(hori.getTerm(), t.getTerm()) + ")");
                    }
                }
                System.out.println("=== 금리 디버깅 끝 ===");
            }
        }
        System.out.println("총 후보군 개수: " + totalCandidates);

        // allTerms 전체 출력
        System.out.println("=== allTerms 전체 단어 ===");
        for (Term t : allTerms) {
            System.out.println("id=" + t.getId() + ", term=" + t.getTerm() + ", glossaries=" + (t.getGlossaries() == null ? 0 : t.getGlossaries().size()));
        }
        System.out.println("총 allTerms 단어 수: " + allTerms.size());

        // 3. 퍼즐 보드 준비 (21x21)
        int width = 21, height = 21;
        char[][] board = new char[height][width];
        for (char[] row : board) Arrays.fill(row, '.');

        // 4. 각 세트별로 보드에 배치 (새로운 알고리즘)
        List<PlacedWord> placed = new ArrayList<>();
        Set<Long> placedTermIds = new HashSet<>();
        int number = 1;
        int currentRow = 1; // 시작 위치 (여유 공간 고려)
        int currentCol = 1;
        
        // 배치된 세트들의 영역을 추적
        List<int[]> placedAreas = new ArrayList<>(); // [startRow, startCol, endRow, endCol]
        
        for (int setIdx = 0; setIdx < userTerms.size(); setIdx++) {
            Term hori = userTerms.get(setIdx);
            List<Term> verticals = verticalTermsList.get(setIdx);
            int horiLen = hori.getTerm().length();
            
            // 금리 단어 배치 과정 추적
            if (hori.getTerm().equals("금리")) {
                System.out.println("=== 금리 배치 과정 ===");
                System.out.println("금리 후보군 개수: " + verticals.size());
                for (Term v : verticals) {
                    System.out.println("  후보: " + v.getTerm());
                }
            }
            
            // 첫 번째 세로 단어 선택 (1번째 글자와 겹치는 단어)
            Term selectedVert1 = null;
            int overlapHoriPos1 = -1;
            int overlapVertPos1 = -1;
            
            if (verticals.size() > 0) {
                selectedVert1 = verticals.get(0);
                // 1번째 글자와 겹치는 위치 찾기
                char firstChar = hori.getTerm().charAt(0);
                String vWord = selectedVert1.getTerm();
                for (int j = 0; j < vWord.length(); j++) {
                    if (vWord.charAt(j) == firstChar) {
                        overlapHoriPos1 = 0;
                        overlapVertPos1 = j;
                        break;
                    }
                }
            }
            
            if (selectedVert1 == null) {
                System.out.println("[" + hori.getTerm() + "] 세트: 겹치는 첫 번째 세로 단어 없음");
                continue;
            }
            // 첫 번째 세로 겹침 위치를 못 찾은 경우 스킵
            if (overlapVertPos1 < 0) {
                System.out.println("[" + hori.getTerm() + "] 세트: 첫 글자 겹침 위치를 찾지 못함 → 세트 스킵");
                continue;
            }
            
            // 두 번째 세로 단어 선택 (3번째 글자 또는 2번째 글자와 겹치는 단어)
            Term selectedVert2 = null;
            int overlapHoriPos2 = -1;
            int overlapVertPos2 = -1;
            
            if (verticals.size() > 1) {
                selectedVert2 = verticals.get(1);
                // 3글자 이상이면 3번째 글자, 2글자면 2번째 글자와 겹치는 위치 찾기
                int targetPos = hori.getTerm().length() >= 3 ? 2 : 1;
                char targetChar = hori.getTerm().charAt(targetPos);
                String vWord = selectedVert2.getTerm();
                for (int j = 0; j < vWord.length(); j++) {
                    if (vWord.charAt(j) == targetChar) {
                        overlapHoriPos2 = targetPos;
                        overlapVertPos2 = j;
                        break;
                    }
                }
                // 두 번째 세로 겹침 위치를 못 찾으면 제외
                if (overlapVertPos2 < 0) {
                    System.out.println("[" + hori.getTerm() + "] 세트: 두 번째 겹침 위치를 찾지 못해 두 번째 세로 단어 제외");
                    selectedVert2 = null;
                }
            }
            
            // 세트 크기 계산을 위한 기본 값 (너비는 고정)
            int vertLen1 = selectedVert1.getTerm().length();
            int vertLen2 = selectedVert2 != null ? selectedVert2.getTerm().length() : 0;
            int setWidth = horiLen;
            
            // 배치 가능한 위치 찾기 (겹침 체크 포함)
            boolean setPlaced = false;
            int maxAttempts = 100; // 무한 루프 방지
            int attempts = 0;
            
            while (!setPlaced && attempts < maxAttempts) {
                attempts++;
                
                // 현재 시도 위치 기준으로 교차/시작행 동적으로 계산
                int acrossRow = currentRow + overlapVertPos1;
                int v1StartRow = currentRow;
                int v2StartRow = -1;
                if (selectedVert2 != null) {
                    v2StartRow = acrossRow - overlapVertPos2;
                }
                int setStartRow = v1StartRow;
                int setEndRow = v1StartRow + vertLen1 - 1;
                if (selectedVert2 != null) {
                    setStartRow = Math.min(setStartRow, v2StartRow);
                    setEndRow = Math.max(setEndRow, v2StartRow + vertLen2 - 1);
                }
                int setHeight = setEndRow - setStartRow + 1;

                // 보드 가로 경계 확인
                if (currentCol + setWidth > width - 1) {
                    // 다음 줄로 이동 (세트 높이만큼 아래로)
                    currentRow = Math.max(currentRow + setHeight + 1, 1);
                    currentCol = 1;
                    continue;
                }

                // 보드 세로 경계 확인
                if (setStartRow < 0 || setEndRow > height - 1) {
                    // 아래로 한 칸 이동하여 재시도
                    currentRow++;
                    continue;
                }
                
                // 기존 배치된 세트들과 겹치는지 확인
                boolean overlaps = false;
                int newSetStartRow = setStartRow;
                int newSetStartCol = currentCol;
                int newSetEndRow = setEndRow;
                int newSetEndCol = currentCol + setWidth - 1;
                
                for (int[] area : placedAreas) {
                    int existingStartRow = area[0];
                    int existingStartCol = area[1];
                    int existingEndRow = area[2];
                    int existingEndCol = area[3];
                    
                    // 겹침 체크 (여유 공간 포함)
                    if (!(newSetEndRow < existingStartRow - 1 || newSetStartRow > existingEndRow + 1 ||
                          newSetEndCol < existingStartCol - 1 || newSetStartCol > existingEndCol + 1)) {
                        overlaps = true;
                        break;
                    }
                }
                
                if (overlaps) {
                    // 겹치면 다음 위치로 이동
                    currentCol += 1;
                    continue;
                }
                
                // 겹치지 않으면 배치 (필수 겹침 인덱스 유효성 확인)
                if (overlapHoriPos1 < 0 || overlapVertPos1 < 0) {
                    // 안전 가드
                    currentCol += 1;
                    continue;
                }
                // 두 번째 단어가 있다면 겹침 인덱스도 유효해야 함
                if (selectedVert2 != null && (overlapHoriPos2 < 0 || overlapVertPos2 < 0)) {
                    // 유효하지 않으면 두 번째는 제외
                    selectedVert2 = null;
                }
                // 겹치지 않으면 배치
                setPlaced = true;
                
                // 가로 단어 배치 (보정된 acrossRow 사용)
                for (int i = 0; i < horiLen; i++) {
                    int rr = acrossRow;
                    int cc = currentCol + i;
                    if (rr < 0 || rr >= height || cc < 0 || cc >= width) continue; // 안전 가드
                    board[rr][cc] = hori.getTerm().charAt(i);
                }
                placed.add(new PlacedWord(hori, acrossRow, currentCol, "across", number++, -1, newSetStartRow, newSetStartCol));
                placedTermIds.add(hori.getId());
                
                // 첫 번째 세로 단어 배치
                for (int j = 0; j < vertLen1; j++) {
                    int rr = v1StartRow + j;
                    int cc = currentCol + overlapHoriPos1;
                    if (rr < 0 || rr >= height || cc < 0 || cc >= width) continue; // 안전 가드
                    board[rr][cc] = selectedVert1.getTerm().charAt(j);
                }
                placed.add(new PlacedWord(selectedVert1, v1StartRow, currentCol + overlapHoriPos1, "down", number++, overlapVertPos1, newSetStartRow, newSetStartCol, 
                    hori.getTerm() + "-" + hori.getTerm().charAt(overlapHoriPos1)));
                placedTermIds.add(selectedVert1.getId());
                
                // 두 번째 세로 단어 배치 (있는 경우)
                if (selectedVert2 != null) {
                    for (int j = 0; j < vertLen2; j++) {
                        int rr = v2StartRow + j;
                        int cc = currentCol + overlapHoriPos2;
                        if (rr < 0 || rr >= height || cc < 0 || cc >= width) continue; // 안전 가드
                        board[rr][cc] = selectedVert2.getTerm().charAt(j);
                    }
                    placed.add(new PlacedWord(selectedVert2, v2StartRow, currentCol + overlapHoriPos2, "down", number++, overlapVertPos2, newSetStartRow, newSetStartCol,
                        hori.getTerm() + "-" + hori.getTerm().charAt(overlapHoriPos2)));
                    placedTermIds.add(selectedVert2.getId());
                }
                

                
                // 배치된 영역 기록
                placedAreas.add(new int[]{newSetStartRow, newSetStartCol, newSetEndRow, newSetEndCol});
                
                // 금리 세트 배치 정보 출력
                if (hori.getTerm().equals("금리")) {
                    System.out.println("금리 세트 배치 성공:");
                    System.out.println("  가로 단어: " + hori.getTerm() + " at (" + (currentRow + overlapVertPos1) + ", " + currentCol + ")");
                    System.out.println("  첫 번째 세로 단어: " + selectedVert1.getTerm() + " at (" + currentRow + ", " + (currentCol + overlapHoriPos1) + ")");
                    System.out.println("  첫 번째 겹치는 글자: '" + hori.getTerm().charAt(overlapHoriPos1) + "' at (" + (currentRow + overlapVertPos1) + ", " + (currentCol + overlapHoriPos1) + ")");
                    if (selectedVert2 != null) {
                        System.out.println("  두 번째 세로 단어: " + selectedVert2.getTerm() + " at (" + currentRow + ", " + (currentCol + overlapHoriPos2) + ")");
                        System.out.println("  두 번째 겹치는 글자: '" + hori.getTerm().charAt(overlapHoriPos2) + "' at (" + (currentRow + overlapVertPos2) + ", " + (currentCol + overlapHoriPos2) + ")");
                    }
                    System.out.println("  세트 크기: " + setWidth + "x" + setHeight);
                    System.out.println("  배치 영역: (" + newSetStartRow + "," + newSetStartCol + ") ~ (" + newSetEndRow + "," + newSetEndCol + ")");
                }
                
                // 다음 세트 위치로 이동
                currentCol += setWidth + 1; // 여유 공간 포함
            }
            
            if (!setPlaced) {
                System.out.println("[" + hori.getTerm() + "] 세트: 배치 실패 (시도 횟수: " + attempts + ")");
            }
        }

        // 5. 결과 변환 (실제 배치된 단어만)
        System.out.println("=== 실제 배치된 단어 ===");
        List<CrosswordItem> result = new ArrayList<>();
        Set<Long> already = new HashSet<>();
        for (PlacedWord pw : placed) {
            if (already.contains(pw.term.getId())) continue;
            already.add(pw.term.getId());
            String clue = pw.term.getGlossaries().get(0).getShortDefinition();
            result.add(new CrosswordItem(
                pw.row, pw.col, pw.length, pw.direction, pw.term.getTerm(), clue, pw.number
            ));
            System.out.println("배치됨: " + pw.term.getTerm() + " (" + pw.direction + ")");
        }
        System.out.println("총 배치된 단어 수: " + result.size());
        
        List<QuizItemDto> quizItems = result.stream().map(item -> {
            QuizItemDto dto = new QuizItemDto();
            dto.setRow(item.row);
            dto.setCol(item.col);
            dto.setLength(item.length);
            dto.setDirection(item.direction);
            dto.setTerm(item.term);
            dto.setQuestion(item.clue);
            dto.setNumber(item.number);
            dto.setInitialHint(KoreanInitialExtractor.extractInitials(item.term));
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

    // 두 단어 간 공통 문자 추출
    private String getCommonChars(String a, String b) {
        StringBuilder common = new StringBuilder();
        for (char c : a.toCharArray()) {
            if (b.indexOf(c) >= 0) {
                common.append(c);
            }
        }
        return common.toString();
    }

    // 2) 사용자 퀴즈 응답을 채점 & 결과 저장
    @Transactional
    public QuizResultDto submitQuiz(Long quizId, Long userId, Map<Long, String> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // 현재 진행 중인 사용자 조회 (정답 처리 시 단어장 삭제에 사용)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        int score = 0;
        Map<Long, Boolean> correctMap = new HashMap<>();
        if (answers == null) {
            System.out.println("[submitQuiz] 경고: answers=null → 빈 맵으로 대체");
            answers = new HashMap<>();
        }
        System.out.println("[submitQuiz] start - quizId=" + quizId + ", userId=" + userId + ", terms=" + quiz.getQuizTerms().size());


        for (QuizTerm qt : quiz.getQuizTerms()) {
            Long termId = qt.getTerm().getId();
            String userAnswer = answers.get(termId); // termId 키로 전달됨
            String correctAnswer = qt.getTerm().getTerm();

            String normalizedUser = Normalizer.normalize(userAnswer == null ? "" : userAnswer);
            String normalizedCorrect = Normalizer.normalize(correctAnswer);

            boolean correct = userAnswer != null && !userAnswer.isEmpty() && normalizedUser.equals(normalizedCorrect);

            int level = qt.getTerm().getFrequency() != null ? qt.getTerm().getFrequency() : 1;
            if (correct) {
                score += level * 10;
                try {
                    // 정답인 단어는 단어장에서 제거하여 다음 퀴즈에 나오지 않도록 처리
                    userVocabularyRepository.deleteByUserAndTerm(user, qt.getTerm());
                } catch (Exception e) {
                    System.err.println("[submitQuiz] uservoca 삭제 실패: userId=" + userId + ", termId=" + termId + ", error=" + e.getMessage());
                }
            }
            correctMap.put(termId, correct); // 여기 주의! qt.getId()가 아니라 termId
            int earned = correct ? level * 10 : 0;
            System.out.println("[submitQuiz] termId=" + termId + ", term='" + correctAnswer + "', level=" + level + 
                               ", userAnswer='" + userAnswer + "', correct=" + correct + ", earned=" + earned + ", accScore=" + score);
        }
        System.out.println("[submitQuiz] total score=" + score);

        // 결과 저장
        QuizResult result = new QuizResult();
        result.setQuiz(quiz);
        result.setUser(user);
        result.setScore(score);
        result.setTakenAt(LocalDateTime.now());
        quizResultRepository.save(result);
        System.out.println("[submitQuiz] result saved - score=" + score + ", takenAt=" + result.getTakenAt());

        // 포인트 업데이트
        int latestTotalPoints = 0;
        if (score > 0) {
            UserPoint userPoint = userPointRepository.findByUser(user);
            System.out.println("[submitQuiz] before point update - current=" + (userPoint == null ? 0 : userPoint.getTotalPoint()) + ", add=" + score);
            
            if (userPoint == null) {
                // 새로운 포인트 레코드 생성
                userPoint = new UserPoint();
                userPoint.setUser(user);
                userPoint.setTotalPoint(score);
                userPoint.setAmount(score);
                userPoint.setReason("퀴즈 정답");
                userPoint.setTimestamp(LocalDateTime.now());
            } else {
                // 기존 포인트에 추가
                int newTotal = userPoint.getTotalPoint() + score;
                userPoint.setTotalPoint(newTotal);
                userPoint.setAmount(score);
                userPoint.setReason("퀴즈 정답");
                userPoint.setTimestamp(LocalDateTime.now());
            }
            
            userPointRepository.save(userPoint);
            latestTotalPoints = userPoint.getTotalPoint();
            System.out.println("[submitQuiz] after point update - total=" + latestTotalPoints);

            // 포인트 업데이트 후 즉시 뱃지 평가 및 부여
            try {
                System.out.println("[submitQuiz] evaluate badges for userId=" + user.getId());
                badgeGrantService.evaluateAndGrantBadges(user.getId());
                System.out.println("[submitQuiz] evaluate badges done");
            } catch (Exception e) {
                System.err.println("뱃지 평가/부여 중 오류: " + e.getMessage());
            }
        }

        System.out.println("[submitQuiz] response - score=" + score + ", totalPoints=" + latestTotalPoints);
        return new QuizResultDto(
                quiz.getId(),
                user.getId(),
                score,
                result.getTakenAt(),
                correctMap,
                latestTotalPoints
        );
    }


    // 3) 가로세로 퀴즈 채점 & 포인트/뱃지 업데이트
    @Transactional
    public Project.Finance_News.dto.CrosswordResultDto submitCrossword(Long userId, Map<String, String> answers) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        int score = 0;
        Map<String, Boolean> results = new HashMap<>();
        if (answers == null) answers = new HashMap<>();

        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String termText = entry.getKey();
            String userAnswer = entry.getValue();
            if (termText == null || termText.isBlank()) continue;

            // 정답 단어 조회
            Term term = termRepository.findByTerm(termText).orElse(null);
            if (term == null) continue;

            String normalizedUser = Normalizer.normalize(userAnswer == null ? "" : userAnswer);
            String normalizedCorrect = Normalizer.normalize(term.getTerm());
            boolean correct = userAnswer != null && !userAnswer.isEmpty() && normalizedUser.equals(normalizedCorrect);
            results.put(termText, correct);
            
            if (correct) {
                int level = term.getFrequency() != null ? term.getFrequency() : 1;
                score += level * 10;
                
                // 정답 처리 시 단어장에서 해당 단어 삭제
                try {
                    userVocabularyRepository.deleteByUserAndTerm(user, term);
                } catch (Exception e) {
                    System.err.println("[submitCrossword] 단어장 삭제 중 오류: " + e.getMessage());
                }
            }
        }

        int latestTotalPoints = 0;
        if (score > 0) {
            UserPoint userPoint = userPointRepository.findByUser(user);
            if (userPoint == null) {
                userPoint = new UserPoint();
                userPoint.setUser(user);
                userPoint.setTotalPoint(score);
                userPoint.setAmount(score);
                userPoint.setReason("가로세로 퀴즈 정답");
                userPoint.setTimestamp(LocalDateTime.now());
            } else {
                int newTotal = userPoint.getTotalPoint() + score;
                userPoint.setTotalPoint(newTotal);
                userPoint.setAmount(score);
                userPoint.setReason("가로세로 퀴즈 정답");
                userPoint.setTimestamp(LocalDateTime.now());
            }
            userPointRepository.save(userPoint);
            latestTotalPoints = userPoint.getTotalPoint();

            try {
                badgeGrantService.evaluateAndGrantBadges(user.getId());
            } catch (Exception e) {
                System.err.println("[submitCrossword] 뱃지 평가 중 오류: " + e.getMessage());
            }
        }

        Project.Finance_News.dto.CrosswordResultDto dto = new Project.Finance_News.dto.CrosswordResultDto();
        dto.setScore(score);
        dto.setTotalPoints(latestTotalPoints);
        dto.setResults(results);
        return dto;
    }
}