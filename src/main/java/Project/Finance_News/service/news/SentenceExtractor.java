package Project.Finance_News.service.news;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import java.io.InputStream;
import java.util.Arrays;

public class SentenceExtractor {
    private static SentenceModel sentenceModel;
    private static SentenceDetectorME sentenceDetector;

    static {
        try {
            // 한국어 모델 파일은 resources에 위치해야 함 (예: models/ko-sent.bin)
            InputStream modelIn = SentenceExtractor.class.getResourceAsStream("/models/ko-sent.bin");
            sentenceModel = new SentenceModel(modelIn);
            sentenceDetector = new SentenceDetectorME(sentenceModel);
        } catch (Exception e) {
            throw new RuntimeException("OpenNLP SentenceModel 로드 실패", e);
        }
    }

    /**
     * 본문에서 단어가 포함된 첫 문장을 반환
     */
    public static String extractSentenceWithWord(String content, String word) {
        if (content == null || word == null) return null;
        String[] sentences = sentenceDetector.sentDetect(content);
        return Arrays.stream(sentences)
                .filter(s -> s.contains(word))
                .findFirst()
                .orElse("");
    }
} 