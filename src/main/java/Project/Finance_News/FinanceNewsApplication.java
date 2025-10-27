package Project.Finance_News;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinanceNewsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceNewsApplication.class, args);
	}
}
