package Project.Finance_News;

import Project.Finance_News.domain.Badge;
import Project.Finance_News.domain.BadgeType;
import Project.Finance_News.domain.Term;
import Project.Finance_News.domain.User;
import Project.Finance_News.domain.UserBadge;
import Project.Finance_News.domain.UserPoint;
import Project.Finance_News.domain.UserVocabulary;
import Project.Finance_News.repository.BadgeRepository;
import Project.Finance_News.repository.TermRepository;
import Project.Finance_News.repository.UserBadgeRepository;
import Project.Finance_News.repository.UserPointRepository;
import Project.Finance_News.repository.UserRepository;
import Project.Finance_News.repository.UserVocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final BadgeRepository badgeRepository;
    private final UserPointRepository userPointRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * 테스트용 데이터 추가
     * */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
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

        User savedUser1 = null;
        User savedUser2 = null;
        
        if (!userRepository.existsByLoginId(user1.getLoginId())) {
            savedUser1 = userRepository.save(user1);
        } else {
            savedUser1 = userRepository.findByLoginId(user1.getLoginId()).orElse(null);
        }
        
        if (!userRepository.existsByLoginId(user2.getLoginId())) {
            savedUser2 = userRepository.save(user2);
        } else {
            savedUser2 = userRepository.findByLoginId(user2.getLoginId()).orElse(null);
        }
        
        // 뱃지 데이터 초기화
        initBadges();
        
        // user_vocabulary 데이터 초기화 (user1에 대해)
        if (savedUser1 != null) {
            initUserVocabulary(savedUser1);
        }
        
        // 샘플 사용자 데이터 초기화 (등급별 사용자)
        initSampleUsers();
    }
    
    private void initBadges() {
        // 뱃지가 이미 존재하는지 확인
        if (badgeRepository.count() > 0) {
            return; // 이미 뱃지가 있으면 초기화하지 않음
        }
        
        // 브론즈 뱃지
        Badge bronze = new Badge();
        bronze.setName("브론즈");
        bronze.setType(BadgeType.RANK_POINT);
        bronze.setDescription("기본 등급");
        bronze.setImageUrl("/img/bronze.png");
        bronze.setConditionValue(0);
        badgeRepository.save(bronze);
        
        // 실버 뱃지
        Badge silver = new Badge();
        silver.setName("실버");
        silver.setType(BadgeType.RANK_POINT);
        silver.setDescription("포인트 100점 달성");
        silver.setImageUrl("/img/silver.png");
        silver.setConditionValue(500);
        badgeRepository.save(silver);
        
        // 골드 뱃지
        Badge gold = new Badge();
        gold.setName("골드");
        gold.setType(BadgeType.RANK_POINT);
        gold.setDescription("포인트 500점 달성");
        gold.setImageUrl("/img/gold.png");
        gold.setConditionValue(1000);
        badgeRepository.save(gold);
        
        // 플래티넘 뱃지
        Badge platinum = new Badge();
        platinum.setName("플래티넘");
        platinum.setType(BadgeType.RANK_POINT);
        platinum.setDescription("포인트 2000점 달성");
        platinum.setImageUrl("/img/platinum.png");
        platinum.setConditionValue(2000);
        badgeRepository.save(platinum);
        
        System.out.println("뱃지 데이터 초기화 완료");
    }
    
    private void initUserVocabulary(User user) {
        // 이미 user_vocabulary가 있는지 확인 후 초기화(삭제 후 재생성)
        List<UserVocabulary> existingVocab = userVocabularyRepository.findByUserId(user.getId());
        if (!existingVocab.isEmpty()) {
            userVocabularyRepository.deleteAll(existingVocab);
        }
        
        // 테스트용 단어들 (모두 3글자 이상)
        String[] testTerms = {"인플레이션", "디플레이션", "통화정책", "재정정책", "경제성장"};
        String[] contextSentences = {
            "글로벌 인플레이션 압력이 지속되면서 중앙은행들의 정책 대응이 주목받고 있다.",
            "수요 위축으로 디플레이션 우려가 커지며 투자 심리가 냉각되고 있다.",
            "통화정책 완화 기조가 시장 유동성 증가로 이어지고 있다.",
            "확장적 재정정책이 경기부양에 기여하고 있다.",
            "신산업 육성과 투자 확대가 경제성장을 견인하고 있다."
        };
        String[] newsTitles = {
            "글로벌 인플레이션 압력 지속",
            "디플레이션 우려 확대",
            "통화정책 완화, 시장 유동성 확대",
            "재정정책 확대로 경기 부양",
            "신산업 투자로 경제성장 견인"
        };
        
        for (int i = 0; i < testTerms.length; i++) {
            Optional<Term> termOpt = termRepository.findByTerm(testTerms[i]);
            if (termOpt.isPresent()) {
                Term term = termOpt.get();
                UserVocabulary vocab = new UserVocabulary();
                vocab.setUser(user);
                vocab.setTerm(term);
                vocab.setFamiliarityLevel(1);
                vocab.setLastSeen(java.time.LocalDate.now());
                vocab.setStarred(false);
                vocab.setContextSentence(contextSentences[i]);
                vocab.setNewsTitle(newsTitles[i]);
                vocab.setNewsUrl("https://example.com/news" + (i + 1));
                vocab.setCreatedAt(java.time.LocalDateTime.now());
                
                userVocabularyRepository.save(vocab);
            }
        }
        
        System.out.println("user_vocabulary 데이터 초기화 완료");
    }
    
    /**
     * 샘플 사용자 데이터 초기화 (등급별 사용자)
     * 티어 기준: 브론즈(<100), 실버(≥100), 골드(≥500), 플래티넘(≥1000)
     */
    private void initSampleUsers() {
        // 1. 브론즈 등급 사용자: 코딩좋아 (10% 진행률 = 10점, 실버까지의 10%)
        User userBronze = createSampleUser("codinglover", "1234", "코딩좋아", "코딩좋아");
        if (userBronze != null) {
            createUserPoint(userBronze, 10, "초기 포인트");
            grantBadgeForUser(userBronze, "브론즈");
        }
        
        // 2. 실버 등급 사용자: 개발자1 (20% 진행률 = 180점, 골드까지의 20%)
        // 실버(100)에서 골드(500)까지의 20% = 100 + (500-100)*0.2 = 180점
        User userSilver = createSampleUser("dev1", "1234", "개발자1", "개발자1");
        if (userSilver != null) {
            createUserPoint(userSilver, 180, "초기 포인트");
            grantBadgeForUser(userSilver, "브론즈");
            grantBadgeForUser(userSilver, "실버");
        }
        
        // 3. 골드 등급 사용자: 플래티넘을향해 (50% 진행률 = 750점, 플래티넘까지의 50%)
        // 골드(500)에서 플래티넘(1000)까지의 50% = 500 + (1000-500)*0.5 = 750점
        User userGold = createSampleUser("platinum_seeker", "1234", "플래티넘을향해", "플래티넘을향해");
        if (userGold != null) {
            createUserPoint(userGold, 750, "초기 포인트");
            grantBadgeForUser(userGold, "브론즈");
            grantBadgeForUser(userGold, "실버");
            grantBadgeForUser(userGold, "골드");
        }
        
        // 4. 플래티넘 등급 사용자: 마라탕좋아 (2500점)
        User userPlatinum = createSampleUser("maratang_lover", "1234", "마라탕좋아", "마라탕좋아");
        if (userPlatinum != null) {
            createUserPoint(userPlatinum, 2500, "초기 포인트");
            grantBadgeForUser(userPlatinum, "브론즈");
            grantBadgeForUser(userPlatinum, "실버");
            grantBadgeForUser(userPlatinum, "골드");
            grantBadgeForUser(userPlatinum, "플래티넘");
        }
        
        System.out.println("샘플 사용자 데이터 초기화 완료");
    }
    
    /**
     * 샘플 사용자 생성
     */
    private User createSampleUser(String loginId, String password, String name, String nickname) {
        // 이미 존재하는지 확인
        if (userRepository.existsByLoginId(loginId)) {
            return userRepository.findByLoginId(loginId).orElse(null);
        }
        
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(password);
        user.setName(name);
        user.setNickname(nickname);
        user.setJob("학생");
        user.setGoal("금융 지식 향상을 위한 학습");
        
        return userRepository.save(user);
    }
    
    /**
     * 사용자 포인트 생성
     */
    private void createUserPoint(User user, int totalPoints, String reason) {
        // 기존 포인트가 있으면 삭제 후 재생성
        UserPoint existingPoint = userPointRepository.findByUser(user);
        if (existingPoint != null) {
            return; // 이미 포인트가 있으면 생성하지 않음
        }
        
        UserPoint userPoint = new UserPoint();
        userPoint.setUser(user);
        userPoint.setTotalPoint(totalPoints);
        userPoint.setAmount(totalPoints);
        userPoint.setReason(reason);
        userPoint.setTimestamp(java.time.LocalDateTime.now());
        
        userPointRepository.save(userPoint);
    }
    
    /**
     * 사용자에게 특정 배지 부여
     */
    private void grantBadgeForUser(User user, String badgeName) {
        List<Badge> allBadges = badgeRepository.findAll();
        Badge badge = allBadges.stream()
                .filter(b -> badgeName.equals(b.getName()))
                .findFirst()
                .orElse(null);
        
        if (badge != null && !userBadgeRepository.existsByUserAndBadge(user, badge)) {
            UserBadge userBadge = new UserBadge();
            userBadge.setUser(user);
            userBadge.setBadge(badge);
            userBadge.setGrantedAt(java.time.LocalDateTime.now());
            userBadgeRepository.save(userBadge);
        }
    }
}
