package com.goormi.routine.domain.scheduler.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.goormi.routine.domain.scheduler.repository.SchedulerRedisRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlySchedulerServiceImpl implements MonthlySchedulerService {

	private final SchedulerManagementService schedulerManagementService;
	private final SchedulerRedisRepository schedulerRedisRepository;

	// 매월 1일 00:30에 월단위 초기화 실행
	@Override
	@Scheduled(cron = "0 30 0 1 * ?", zone = "Asia/Seoul")
	public void executeMonthlyReset() {
		LocalDateTime startTime = LocalDateTime.now();

		try {
			schedulerRedisRepository.updateSchedulerStatus("monthly_reset", "RUNNING", "월간 초기화 시작");

			schedulerManagementService.executeMonthlyReset();

			schedulerRedisRepository.updateSchedulerStatus("monthly_reset", "SUCCESS", "월간 초기화 완료");
			log.info("자동 월간 초기화 완료");

		} catch (Exception e) {
			String errorMsg = "월간 초기화 실패: " + e.getMessage();
			schedulerRedisRepository.updateSchedulerStatus("monthly_reset", "FAILED", errorMsg);
			log.error("자동 월간 초기화 실패", e);
		}
	}

	@Override
	@Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Seoul")
	public void retryFailedReviewMessages () {
		try {
			// 월초 3일간만 실행
			int dayOfMonth = java.time.LocalDate.now().getDayOfMonth();

			if (dayOfMonth <= 3) {
				schedulerRedisRepository.updateSchedulerStatus("retry_message", "RUNNING", "재전송 스케줄러 시작");
				String previousMonth = java.time.LocalDate.now().minusMonths(1)
					.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

				schedulerManagementService.retryFailedMessages(previousMonth);

				schedulerRedisRepository.updateSchedulerStatus("retry_message", "SUCCESS", "재전송 완료");
				log.info("회고 메시지 재전송 완료");
			}

		} catch (Exception e) {
			String errorMsg = "재전송 실패: " + e.getMessage();
			schedulerRedisRepository.updateSchedulerStatus("retry_message", "FAILED", errorMsg);
			log.error("회고 메시지 재전송 실패", e);
		}
	}

	// 수동 실행용 메서드들
	@Override
	public void manualMonthlyReset() {
		log.info("수동 월간 초기화 실행");
		schedulerManagementService.executeMonthlyReset();
	}

	@Override
	public void manualRetryReviewMessages(String monthYear){
		log.info("수동 회고 메시지 재전송 실행: 월 = {}", monthYear);
		schedulerManagementService.retryFailedMessages(monthYear);
	}
}