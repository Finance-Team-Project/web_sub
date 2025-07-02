package Project.Finacne_News.service.news;

import Project.Finacne_News.domain.Badge;
import Project.Finacne_News.domain.User;
import Project.Finacne_News.domain.UserBadge;
import Project.Finacne_News.domain.UserPoint;
import Project.Finacne_News.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void evaluateAndGrantBadges(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Badge> allBadges = badgeRepository.findAll();

        for (Badge badge : allBadges) {
            boolean alreadyGranted = userBadgeRepository.existsByUserAndBadge(user, badge);
            if (alreadyGranted) continue;

            boolean grant = switch (badge.getType()) {
                case FIRST_CORRECT -> hasFirstCorrectAnswer(user);
                case CORRECT_COUNT -> hasCorrectCount(user, badge.getConditionValue());
                case USER_POINT -> hasEnoughPoints(user, badge.getConditionValue());
                case QUIZ_COUNT -> hasQuizCount(user, badge.getConditionValue());
            };

            if (grant) {
                UserBadge userBadge = new UserBadge();
                userBadge.setUser(user);
                userBadge.setBadge(badge);
                userBadgeRepository.save(userBadge);
            }
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

