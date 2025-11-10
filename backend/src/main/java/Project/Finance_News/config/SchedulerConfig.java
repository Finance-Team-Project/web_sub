package Project.Finance_News.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {
    private final JobLauncher jobLauncher;
    private final Job newsProcessingJob;

    @Scheduled(cron = "${batch.schedule.news.cron}")  // application.properties에서 설정
    public void runNewsProcessingJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("datetime", LocalDateTime.now().toString()) // key value형태로 파라미터 추가
                    .toJobParameters();

            log.info("Starting news processing job: {}", LocalDateTime.now());
            jobLauncher.run(newsProcessingJob, jobParameters);
            
        } catch (Exception e) {
            log.error("Error during news processing job execution: ", e);
        }
    }

    // 수동 실행을 위한 메서드
    public void runJobManually() {
        runNewsProcessingJob();
    }
}
