package Project.Finance_News.config;

import Project.Finance_News.domain.News;
import Project.Finance_News.repository.NewsRepository;
import Project.Finance_News.repository.NewsKeywordRepository;
import Project.Finance_News.dto.NewsApiResponse;
import Project.Finance_News.dto.NewsDto;
import Project.Finance_News.batch.reader.RestApiReader;
import Project.Finance_News.batch.reader.NewsCollectionReader;
import Project.Finance_News.batch.reader.ProcessedNewsReader;
import Project.Finance_News.batch.processor.NewsProcessor;
import Project.Finance_News.batch.writer.NewsCollectionWriter;
import Project.Finance_News.batch.writer.ProcessedNewsWriter;
import Project.Finance_News.batch.writer.DatabaseNewsWriter;
import Project.Finance_News.batch.holder.NewsCollectionHolder;
import Project.Finance_News.batch.holder.ProcessedNewsHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NewsRepository newsRepository;
    private final NewsKeywordRepository newsKeywordRepository;
    private final NewsCollectionHolder newsCollectionHolder;
    private final ProcessedNewsHolder processedNewsHolder;
    private final RestTemplate restTemplate;  // 주입받도록 변경

    @Bean
    public Job newsProcessingJob() {
        return new JobBuilder("newsProcessingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(crawlNewsStep())
                .next(processNewsStep())
                .next(saveNewsStep())
                .build();
    }

    @Bean
    public Step crawlNewsStep() {
        return new StepBuilder("crawlNewsStep", jobRepository)
                .<NewsApiResponse, NewsApiResponse>chunk(1, transactionManager)
                .reader(restApiReader())
                .writer(newsCollectionWriter())
                .build();
    }

    @Bean
    public Step processNewsStep() {
        return new StepBuilder("processNewsStep", jobRepository)
                .<NewsDto, News>chunk(50, transactionManager)
                .reader(newsCollectionReader())
                .processor(newsProcessor())
                .writer(processedNewsWriter())
                .build();
    }

    @Bean
    public Step saveNewsStep() {
        return new StepBuilder("saveNewsStep", jobRepository)
                .<News, News>chunk(100, transactionManager)
                .reader(processedNewsReader())
                .writer(databaseNewsWriter())
                .build();
    }

    @Bean
    public ItemReader<NewsApiResponse> restApiReader() {
        return new RestApiReader(restTemplate);  // 주입받은 RestTemplate 사용
    }

    @Bean
    public ItemReader<NewsDto> newsCollectionReader() {
        return new NewsCollectionReader(newsCollectionHolder);
    }

    @Bean
    public ItemReader<News> processedNewsReader() {
        return new ProcessedNewsReader(processedNewsHolder);
    }

    @Bean
    public ItemProcessor<NewsDto, News> newsProcessor() {
        return new NewsProcessor(newsRepository);
    }

    @Bean
    public ItemWriter<NewsApiResponse> newsCollectionWriter() {
        return new NewsCollectionWriter(newsCollectionHolder);
    }

    @Bean
    public ItemWriter<News> processedNewsWriter() {
        return new ProcessedNewsWriter(processedNewsHolder);
    }

    @Bean
    public ItemWriter<News> databaseNewsWriter() {
        return new DatabaseNewsWriter(newsRepository, newsKeywordRepository);
    }
}