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
import java.time.LocalDateTime;
import Project.Finance_News.domain.Badge;
import Project.Finance_News.domain.UserBadge;
import Project.Finance_News.domain.UserPoint;
import Project.Finance_News.repository.UserBadgeRepository;
import Project.Finance_News.repository.BadgeRepository;
import Project.Finance_News.repository.UserPointRepository;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final UserPointRepository userPointRepository;

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
}
