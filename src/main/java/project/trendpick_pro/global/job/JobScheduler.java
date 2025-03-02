package project.trendpick_pro.global.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobScheduler {
    private final JobLauncher jobLauncher; //job을 실행시키는 주체
    @Qualifier("makeRecommendProductJob")
    private final Job makeRecommendProductJob;
    @Qualifier("cancelOrderJob")
    private final Job cancelOrderJob;

    @Qualifier("makeRebateDataJob")
    private final Job makeRebateDataJob;


    //매일 03시에 전날에 접속 이력이 있는 회원을 대상으로 추천 상품을 갱신해준다.
    @Scheduled(cron = "0 0 3 * * *")
    public void performMakeRecommendProductJob() throws Exception {

        LocalDateTime fromDate = LocalDateTime.now().minusDays(1).with(LocalTime.MIN); //전날 00:00
        LocalDateTime toDate = LocalDateTime.now().minusDays(1).with(LocalTime.MAX); //전날 11:59
//        fromDate = LocalDateTime.now().minusDays(2); //테스트용
//        toDate = LocalDateTime.now().plusDays(2); //테스트용

        JobParameters param = new JobParametersBuilder()
                .addLocalDateTime("fromDate", fromDate)
                .addLocalDateTime("toDate", toDate)
                .toJobParameters();

        JobExecution execution = jobLauncher.run(makeRecommendProductJob, param);
        log.info(execution.getStatus().toString());
    }

    // 10분마다 체크해서 주문이 생성된지 30분 이상 지났는데 결제 처리가 없으면 주문을 취소상태로 변경한다.
    @Scheduled(fixedRate = 1000 * 60 * 10)
    @Async
    public void performCancelOrderJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        LocalDateTime date = LocalDateTime.now().minusMinutes(30);
//        LocalDateTime date = LocalDateTime.now().plusDays(1); //테스트용

        JobParameters param = new JobParametersBuilder()
                .addLocalDateTime("date", date) //전날 11:59
                .toJobParameters();
        JobExecution execution = jobLauncher.run(cancelOrderJob, param);
        log.info("job result : " + execution.getStatus());

    }

    // 매일 04시에 당월의 주문 목록을 복사해서 정산용 주문 목록으로 만들어 저장한다.
    @Scheduled(cron = "0 0 4 * * *") // 실제 코드
    public void performMakeRebateDataJob() throws Exception {
        String yearMonth = getPerformMakeRebateDataJobParam1Value(); // 실제 코드
        //   String yearMonth = "2023-07"; // 개발용

        JobParameters param = new JobParametersBuilder()
                .addString("yearMonth", yearMonth)
                .toJobParameters();
        JobExecution execution = jobLauncher.run(makeRebateDataJob, param);

        log.info(String.valueOf(execution.getStatus()));
    }

    private String getPerformMakeRebateDataJobParam1Value() {
        LocalDateTime rebateDate = LocalDateTime.now().getDayOfMonth() >= 15 ? LocalDateTime.now().minusMonths(1) : LocalDateTime.now().minusMonths(2);

        return "%04d".formatted(rebateDate.getYear()) + "-" + "%02d".formatted(rebateDate.getMonthValue());
    }
}
