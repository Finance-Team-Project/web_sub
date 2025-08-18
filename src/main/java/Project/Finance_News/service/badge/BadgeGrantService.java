package Project.Finance_News.service.badge;

import Project.Finance_News.domain.*;
import Project.Finance_News.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeGrantService {

    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserPointRepository userPointRepository;
    private final QuizRepository quizRepository;

    @Transactional
    public void evaluateAndGrantBadges(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Badge> allBadges = badgeRepository.findAll();

        // 안전장치: 주요 랭크 배지 임계값을 코드에서 정규화 (DB 초기값 불일치 보정)
        for (Badge b : allBadges) {
            if ("브론즈".equals(b.getName()) && b.getConditionValue() != 0) {
                b.setConditionValue(0);
            } else if ("실버".equals(b.getName()) && b.getConditionValue() != 100) {
                b.setConditionValue(100);
            }
        }

        // 0) 브론즈는 조건 없이 기본 지급
        allBadges.stream()
                .filter(b -> "브론즈".equals(b.getName()))
                .findFirst()
                .ifPresent(b -> grantBadgeIfNotGranted(user, b));

        for (Badge badge : allBadges) {
            if (badge.getType() == BadgeType.RANK_CORRECT || badge.getType() == BadgeType.RANK_POINT)
                continue;
            grantBadgeIfNotGranted(user, badge);
            boolean grant = switch (badge.getType()) {
                case FIRST_CORRECT -> hasFirstCorrectAnswer(user);
                case CORRECT_COUNT -> hasCorrectCount(user, badge.getConditionValue());
                case QUIZ_COUNT -> hasQuizCount(user, badge.getConditionValue());
                case USER_POINT -> hasEnoughPoints(user, badge.getConditionValue());
                default -> false;
            };

            if (grant) {
                grantBadgeIfNotGranted(user, badge);
            }
        }

        // 랭크는 마지막에 처리 (최고 등급만 부여)
        evaluateAndGrantRankBadge(user, allBadges);
    }

    private void evaluateAndGrantRankBadge(User user, List<Badge> allBadges) {
        // RANK_CORRECT
        long correctCount = quizResultRepository.countByUserAndIsCorrectTrue(user);
        allBadges.stream()
                .filter(b -> b.getType() == BadgeType.RANK_CORRECT)
                .sorted(Comparator.comparingInt(Badge::getConditionValue).reversed()) // 높은 조건부터
                .filter(b -> correctCount >= b.getConditionValue())
                .findFirst()
                .ifPresent(b -> grantBadgeIfNotGranted(user, b));

        // RANK_POINT
        UserPoint point = userPointRepository.findByUser(user);
        int total = point != null ? point.getTotalPoint() : 0;
        allBadges.stream()
                .filter(b -> b.getType() == BadgeType.RANK_POINT)
                .sorted(Comparator.comparingInt(Badge::getConditionValue).reversed())
                .filter(b -> total >= b.getConditionValue())
                .findFirst()
                .ifPresent(b -> grantBadgeIfNotGranted(user, b));
    }

    private void grantBadgeIfNotGranted(User user, Badge badge) {
        if (!userBadgeRepository.existsByUserAndBadge(user, badge)) {
            UserBadge userBadge = new UserBadge();
            userBadge.setUser(user);
            userBadge.setBadge(badge);
            userBadge.setGrantedAt(LocalDateTime.now());
            userBadgeRepository.save(userBadge);
        }
    }

    private boolean hasFirstCorrectAnswer(User user) {
        return quizResultRepository.existsByUserAndIsCorrectTrue(user);
    }

    private boolean hasCorrectCount(User user, int threshold) {
        return quizResultRepository.countByUserAndIsCorrectTrue(user) >= threshold;
    }

    private boolean hasEnoughPoints(User user, int requiredPoint) {
        UserPoint userPoint = userPointRepository.findByUser(user);
        return userPoint != null && userPoint.getTotalPoint() >= requiredPoint;
    }

    private boolean hasQuizCount(User user, int requiredCount) {
        return quizRepository.countByUser(user) >= requiredCount;
    }


}

