package Project.Finance_News.controller;

import Project.Finance_News.domain.User;
import Project.Finance_News.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import Project.Finance_News.domain.Badge;
import Project.Finance_News.domain.UserBadge;
import Project.Finance_News.domain.UserPoint;
import Project.Finance_News.repository.UserBadgeRepository;
import Project.Finance_News.repository.BadgeRepository;
import Project.Finance_News.repository.UserPointRepository;
import Project.Finance_News.repository.UserNewsLogRepository;
import jakarta.servlet.http.HttpSession;
import Project.Finance_News.domain.session.SessionConst;
import Project.Finance_News.domain.UserNewsLog;
import Project.Finance_News.domain.News;
import Project.Finance_News.repository.NewsKeywordRepository;
import Project.Finance_News.repository.NewsRepository;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final UserPointRepository userPointRepository;
    private final UserNewsLogRepository userNewsLogRepository;
    private final NewsKeywordRepository newsKeywordRepository;
    private final NewsRepository newsRepository;
    
    @PersistenceContext
    private EntityManager em;

    // BadgeDisplayDto 내부 클래스
    public static class BadgeDisplayDto {
        public String name;
        public String description;
        public String imageUrl;
        public boolean earned;
        public java.time.LocalDateTime grantedAt;
        public int conditionValue;
        
        public BadgeDisplayDto(String name, String description, String imageUrl, boolean earned, java.time.LocalDateTime grantedAt, int conditionValue) {
            this.name = name;
            this.description = description;
            this.imageUrl = imageUrl;
            this.earned = earned;
            this.grantedAt = grantedAt;
            this.conditionValue = conditionValue;
        }
    }

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
        return "redirect:/login";
    }

    @GetMapping("/users/{userId}/badges")
    public String viewUserBadges(@PathVariable Long userId, Model model) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // 사용자가 획득한 뱃지들
        List<UserBadge> earnedBadges = userBadgeRepository.findByUser(user);
        
        // 모든 뱃지들
        List<Badge> allBadges = badgeRepository.findAll();
        
        // 뱃지 정보를 담을 DTO 리스트
        List<BadgeDisplayDto> badgeDisplayList = allBadges.stream().map(badge -> {
            // 사용자가 이 뱃지를 획득했는지 확인
            UserBadge userBadge = earnedBadges.stream()
                    .filter(ub -> ub.getBadge().getId().equals(badge.getId()))
                    .findFirst()
                    .orElse(null);
            
            return new BadgeDisplayDto(
                    badge.getName(),
                    badge.getDescription(),
                    badge.getImageUrl(),
                    userBadge != null,
                    userBadge != null ? userBadge.getGrantedAt() : null,
                    badge.getConditionValue()
            );
        }).toList();
        
        model.addAttribute("userBadges", badgeDisplayList);
        return "users/badges";
    }

    // REST API로 뱃지 데이터 반환
    @GetMapping("/api/users/{userId}/badges")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<BadgeDisplayDto> getUserBadgesApi(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // 사용자가 획득한 뱃지들
        List<UserBadge> earnedBadges = userBadgeRepository.findByUser(user);
        
        // 모든 뱃지들
        List<Badge> allBadges = badgeRepository.findAll();
        
        // 뱃지 정보를 담을 DTO 리스트
        return allBadges.stream().map(badge -> {
            // 사용자가 이 뱃지를 획득했는지 확인
            UserBadge userBadge = earnedBadges.stream()
                    .filter(ub -> ub.getBadge().getId().equals(badge.getId()))
                    .findFirst()
                    .orElse(null);
            
            return new BadgeDisplayDto(
                    badge.getName(),
                    badge.getDescription(),
                    badge.getImageUrl(),
                    userBadge != null,
                    userBadge != null ? userBadge.getGrantedAt() : null,
                    badge.getConditionValue()
            );
        }).toList();
    }
    
    // 사용자 포인트 정보 반환
    @GetMapping("/api/users/{userId}/points")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> getUserPoints(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // 사용자의 총 포인트 계산
        UserPoint userPoint = userPointRepository.findByUser(user);
        int totalPoints = userPoint != null ? userPoint.getTotalPoint() : 0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalPoints", totalPoints);
        response.put("userId", userId);
        
        return response;
    }
    
    // 퀴즈 제출 후 포인트 업데이트
    @PostMapping("/api/users/{userId}/points/update")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> updateUserPoints(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        try {
            System.out.println("포인트 업데이트 요청 받음 - userId: " + userId + ", request: " + request);
            
            User user = userRepository.findById(userId).orElseThrow();
            int earnedPoints = (Integer) request.get("earnedPoints");
            System.out.println("획득한 포인트: " + earnedPoints);
            
            // 기존 포인트 정보 가져오기
            UserPoint userPoint = userPointRepository.findByUser(user);
            System.out.println("기존 포인트 정보: " + userPoint);
            
            if (userPoint == null) {
                // 새로운 포인트 레코드 생성
                userPoint = new UserPoint();
                userPoint.setUser(user);
                userPoint.setTotalPoint(earnedPoints);
                userPoint.setAmount(earnedPoints);
                userPoint.setReason("퀴즈 정답");
                userPoint.setTimestamp(LocalDateTime.now());
                System.out.println("새로운 포인트 레코드 생성: " + userPoint.getTotalPoint());
            } else {
                // 기존 포인트에 추가
                int oldTotal = userPoint.getTotalPoint();
                int newTotal = userPoint.getTotalPoint() + earnedPoints;
                userPoint.setTotalPoint(newTotal);
                userPoint.setAmount(earnedPoints);
                userPoint.setReason("퀴즈 정답");
                userPoint.setTimestamp(LocalDateTime.now());
                System.out.println("기존 포인트 업데이트: " + oldTotal + " -> " + newTotal);
            }
            
            // 포인트 저장
            userPointRepository.save(userPoint);
            System.out.println("포인트 저장 완료");
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalPoints", userPoint.getTotalPoint());
            response.put("earnedPoints", earnedPoints);
            response.put("userId", userId);
            
            System.out.println("응답: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("포인트 업데이트 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // 랭킹 DTO
    public static class RankingDto {
        public Long userId;
        public String nickname;
        public int totalPoints;
        public int rank;
        
        public RankingDto(Long userId, String nickname, int totalPoints, int rank) {
            this.userId = userId;
            this.nickname = nickname;
            this.totalPoints = totalPoints;
            this.rank = rank;
        }
    }
    
    // 랭킹 페이지
    @GetMapping("/ranking")
    public String rankingPage(Model model) {
        return "ranking";
    }
    
    // 랭킹 API
    @GetMapping("/api/ranking")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<RankingDto> getRanking() {
        List<User> allUsers = userRepository.findAll();
        
        List<RankingDto> ranking = new ArrayList<>();
        
        for (User user : allUsers) {
            UserPoint userPoint = userPointRepository.findByUser(user);
            int totalPoints = userPoint != null ? userPoint.getTotalPoint() : 0;
            ranking.add(new RankingDto(user.getId(), user.getNickname(), totalPoints, 0));
        }
        
        // 포인트 기준으로 내림차순 정렬
        ranking = ranking.stream()
                .sorted(Comparator.comparingInt((RankingDto r) -> r.totalPoints).reversed())
                .collect(Collectors.toList());
        
        // 순위 설정
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).rank = i + 1;
        }
        
        return ranking;
    }
    
    // 마이페이지 메인
    @GetMapping("/mypage")
    public String myPage(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            HttpSession session, 
            Model model) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            return "redirect:/login";
        }
        
        // 사용자 정보
        User user = userRepository.findById(loginUser.getId()).orElseThrow();
        
        // 프로필 정보
        UserPoint userPoint = userPointRepository.findByUser(user);
        int totalPoints = userPoint != null ? userPoint.getTotalPoint() : 0;
        
        // 티어 계산 (포인트 기반)
        String tier = calculateTier(totalPoints);
        
        // 최근 본 뉴스 (중복 제거)
        List<UserNewsLog> recentLogs = userNewsLogRepository.findByUserOrderByViewedAtDesc(user);
        List<News> allRecentNews = recentLogs.stream()
                .map(UserNewsLog::getNews)
                .distinct()
                .collect(Collectors.toList());
        
        // 페이지네이션 (페이지당 3개)
        int pageSize = 3;
        int totalNews = allRecentNews.size();
        int totalPages = totalNews > 0 ? (int) Math.ceil((double) totalNews / pageSize) : 0;
        
        List<News> recentNews;
        if (totalNews == 0) {
            recentNews = new ArrayList<>();
        } else {
            int startIndex = Math.min(page * pageSize, totalNews);
            int endIndex = Math.min(startIndex + pageSize, totalNews);
            recentNews = allRecentNews.subList(startIndex, endIndex);
        }
        
        // 뉴스별 요약 및 키워드 정보 준비
        Map<Long, String> newsSummaries = recentNews.stream()
                .filter(n -> n.getNewsSummary() != null)
                .collect(Collectors.toMap(
                    News::getId,
                    n -> n.getNewsSummary().getSummary() != null ? n.getNewsSummary().getSummary() : ""
                ));
        
        Map<Long, List<String>> newsKeywords = recentNews.stream()
                .collect(Collectors.toMap(
                    News::getId,
                    n -> newsKeywordRepository.findKeywordsByNewsId(n.getId())
                            .stream()
                            .distinct()
                            .collect(Collectors.toList())
                ));
        
        // 가장 많이 본 키워드 TOP 3 (전체 뉴스 로그에서)
        Map<String, Long> keywordCounts = recentLogs.stream()
                .flatMap(log -> newsKeywordRepository.findKeywordsByNewsId(log.getNews().getId()).stream())
                .distinct()
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()));
        
        List<String> topKeywords = keywordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 이번 주 학습률 계산 (최근 7일간 본 뉴스 수 / 목표 10개)
        long thisWeekNewsCount = recentLogs.stream()
                .filter(log -> log.getViewedAt() != null && 
                        log.getViewedAt().isAfter(java.time.LocalDateTime.now().minusDays(7)))
                .map(log -> log.getNews().getId())
                .distinct()
                .count();
        int weeklyProgress = Math.min(100, (int)(thisWeekNewsCount * 10)); // 최대 100%
        
        // 사용자 레벨 계산 (포인트 기반)
        int userLevel = calculateLevel(totalPoints);
        int pointsToNextLevel = calculatePointsToNextLevel(totalPoints);
        
        // 사용자가 획득한 뱃지 중 가장 최근에 획득한 뱃지 이미지 가져오기
        List<UserBadge> earnedBadges = userBadgeRepository.findByUser(user);
        String profileBadgeImageUrl = null;
        if (!earnedBadges.isEmpty()) {
            // 가장 최근에 획득한 뱃지 찾기
            UserBadge latestBadge = earnedBadges.stream()
                    .filter(ub -> ub.getGrantedAt() != null)
                    .max(java.util.Comparator.comparing(UserBadge::getGrantedAt))
                    .orElse(earnedBadges.get(0)); // grantedAt이 null인 경우도 있으므로 첫 번째 뱃지 사용
            
            if (latestBadge != null && latestBadge.getBadge() != null) {
                profileBadgeImageUrl = latestBadge.getBadge().getImageUrl();
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("tier", tier);
        model.addAttribute("recentNews", recentNews);
        model.addAttribute("newsSummaries", newsSummaries);
        model.addAttribute("newsKeywords", newsKeywords);
        model.addAttribute("topKeywords", topKeywords);
        model.addAttribute("weeklyProgress", weeklyProgress);
        model.addAttribute("userLevel", userLevel);
        model.addAttribute("pointsToNextLevel", pointsToNextLevel);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalNews", totalNews);
        model.addAttribute("profileBadgeImageUrl", profileBadgeImageUrl);
        
        return "users/mypage";
    }
    
    // 레벨 계산 (포인트 기반)
    private int calculateLevel(int totalPoints) {
        if (totalPoints >= 1000) return 5;
        if (totalPoints >= 500) return 4;
        if (totalPoints >= 200) return 3;
        if (totalPoints >= 100) return 2;
        return 1;
    }
    
    // 다음 레벨까지 필요한 포인트 계산
    private int calculatePointsToNextLevel(int totalPoints) {
        int currentLevel = calculateLevel(totalPoints);
        int nextLevelThreshold;
        if (currentLevel == 1) nextLevelThreshold = 100;
        else if (currentLevel == 2) nextLevelThreshold = 200;
        else if (currentLevel == 3) nextLevelThreshold = 500;
        else if (currentLevel == 4) nextLevelThreshold = 1000;
        else return 0; // 최대 레벨
        return Math.max(0, nextLevelThreshold - totalPoints);
    }
    
    // 티어 계산 (포인트 기반)
    private String calculateTier(int totalPoints) {
        if (totalPoints >= 1000) return "플래티넘";
        if (totalPoints >= 500) return "골드";
        if (totalPoints >= 100) return "실버";
        return "브론즈";
    }
    
    // 닉네임 수정 API
    @PostMapping("/api/users/{userId}/nickname")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> updateNickname(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null || !loginUser.getId().equals(userId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "권한이 없습니다.");
            return error;
        }
        
        String newNickname = request.get("nickname");
        if (newNickname == null || newNickname.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "닉네임을 입력해주세요.");
            return error;
        }
        
        User user = userRepository.findById(userId).orElseThrow();
        user.setNickname(newNickname.trim());
        userRepository.save(user);
        
        // 세션 업데이트
        session.setAttribute(SessionConst.LOGIN_USER, user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("nickname", user.getNickname());
        return response;
    }
    
    // 비밀번호 수정 API
    @PostMapping("/api/users/{userId}/password")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> updatePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null || !loginUser.getId().equals(userId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "권한이 없습니다.");
            return error;
        }
        
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "현재 비밀번호와 새 비밀번호를 입력해주세요.");
            return error;
        }
        
        User user = userRepository.findById(userId).orElseThrow();
        
        // 현재 비밀번호 확인 (평문 비교 - 현재 시스템과 일치)
        if (!currentPassword.equals(user.getPassword())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "현재 비밀번호가 일치하지 않습니다.");
            return error;
        }
        
        // 새 비밀번호 설정 (평문 저장 - 현재 시스템과 일치)
        user.setPassword(newPassword);
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "비밀번호가 변경되었습니다.");
        return response;
    }
    
    // 마이페이지 뉴스 목록 API (AJAX용)
    @GetMapping("/api/mypage/news")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> getMyPageNews(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "로그인이 필요합니다.");
            return error;
        }
        
        User user = userRepository.findById(loginUser.getId()).orElseThrow();
        
        // 최근 본 뉴스 (중복 제거)
        List<UserNewsLog> recentLogs = userNewsLogRepository.findByUserOrderByViewedAtDesc(user);
        List<News> allRecentNews = recentLogs.stream()
                .map(UserNewsLog::getNews)
                .distinct()
                .collect(Collectors.toList());
        
        // 페이지네이션 (페이지당 3개)
        int pageSize = 3;
        int totalNews = allRecentNews.size();
        int totalPages = totalNews > 0 ? (int) Math.ceil((double) totalNews / pageSize) : 0;
        
        List<News> recentNews;
        if (totalNews == 0) {
            recentNews = new ArrayList<>();
        } else {
            int startIndex = Math.min(page * pageSize, totalNews);
            int endIndex = Math.min(startIndex + pageSize, totalNews);
            recentNews = allRecentNews.subList(startIndex, endIndex);
        }
        
        // 뉴스별 요약 및 키워드 정보 준비
        Map<Long, String> newsSummaries = recentNews.stream()
                .filter(n -> n.getNewsSummary() != null)
                .collect(Collectors.toMap(
                    News::getId,
                    n -> n.getNewsSummary().getSummary() != null ? n.getNewsSummary().getSummary() : ""
                ));
        
        Map<Long, List<String>> newsKeywords = recentNews.stream()
                .collect(Collectors.toMap(
                    News::getId,
                    n -> newsKeywordRepository.findKeywordsByNewsId(n.getId())
                            .stream()
                            .distinct()
                            .collect(Collectors.toList())
                ));
        
        // 응답 데이터 구성
        List<Map<String, Object>> newsList = recentNews.stream().map(news -> {
            Map<String, Object> newsData = new HashMap<>();
            newsData.put("id", news.getId());
            newsData.put("title", news.getTitle());
            newsData.put("press", news.getPress());
            newsData.put("publishedAt", news.getPublishedAt());
            newsData.put("imageUrl", news.getImageUrl());
            newsData.put("url", news.getUrl());
            newsData.put("summary", newsSummaries.getOrDefault(news.getId(), ""));
            newsData.put("keywords", newsKeywords.getOrDefault(news.getId(), new ArrayList<>()));
            return newsData;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("news", newsList);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalNews", totalNews);
        
        return response;
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/api/mypage/news/{newsId}")
    @org.springframework.web.bind.annotation.ResponseBody
    @Transactional
    public Map<String, Object> deleteMyPageNews(
            @PathVariable Long newsId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        User user = userRepository.findById(loginUser.getId()).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "사용자 정보를 찾을 수 없습니다.");
            return response;
        }

        News news = newsRepository.findById(newsId).orElse(null);
        if (news == null) {
            response.put("success", false);
            response.put("message", "뉴스 정보를 찾을 수 없습니다.");
            return response;
        }

        // 삭제 전 로그 개수 확인
        long beforeCount = userNewsLogRepository.countByUserAndNews(user, news);
        System.out.println("삭제 전 뉴스 로그 개수: " + beforeCount + " (userId: " + user.getId() + ", newsId: " + newsId + ")");

        long deletedCount = userNewsLogRepository.deleteByUserAndNews(user, news);
        
        // 삭제 후 로그 개수 확인
        long afterCount = userNewsLogRepository.countByUserAndNews(user, news);
        System.out.println("삭제 후 뉴스 로그 개수: " + afterCount + ", 삭제된 개수: " + deletedCount);
        
        if (deletedCount == 0) {
            response.put("success", false);
            response.put("message", "삭제할 뉴스 기록이 없습니다.");
            return response;
        }

        response.put("success", true);
        response.put("deletedCount", deletedCount);
        response.put("beforeCount", beforeCount);
        response.put("afterCount", afterCount);
        return response;
    }
}
