package Project.Finance_News.util;

public class KoreanInitialExtractor {

    // 유니코드 기준: 한글 음절의 시작 위치
    private static final int HANGUL_BASE = 0xAC00;
    private static final int CHOSUNG_INTERVAL = 21 * 28;

    private static final char[] CHOSUNG_LIST = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
            'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
            'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    /**
     * 주어진 문자열에서 한글 초성만 추출한 문자열을 반환
     */
    public static String extractInitials(String input) {
        StringBuilder result = new StringBuilder();

        for (char ch : input.toCharArray()) {
            if (ch >= 0xAC00 && ch <= 0xD7A3) {
                int unicodeIndex = ch - HANGUL_BASE;
                int chosungIndex = unicodeIndex / CHOSUNG_INTERVAL;
                result.append(CHOSUNG_LIST[chosungIndex]);
            } else if (Character.isLetterOrDigit(ch)) {
                // 영문자나 숫자 등도 힌트로 포함하려면 여기 추가
                result.append(ch);
            } else {
                result.append(' '); // 공백이나 특수문자 등은 공백 처리
            }
        }

        return result.toString();
    }
}
