package Project.Finance_News.util;

public class Normalizer {
    public static String normalize(String input) {
        if (input == null) return "";
        return input.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}가-힣]", "")  // 특수문자 제거
                .replaceAll("\\s+", "")  // 공백 제거
                .toLowerCase();         // 소문자로 통일
    }
}