package com.goormi.routine.domain.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.goormi.routine.domain.scheduler.repository.SchedulerRedisRepository;
import com.goormi.routine.domain.ranking.service.RankingService;
import com.goormi.routine.domain.review.service.ReviewService;
import com.goormi.routine.domain.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerManagementServiceImpl implements SchedulerManagementService{

	private final RankingService rankingService;
	private final ReviewService reviewService;
	private final SchedulerRedisRepository schedulerRedisRepository;
	private final UserRepository userRepository;

	@Override
	public void executeMonthlyReset() {
		String previousMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

		reviewService.sendMonthlyReviewMessages(previousMonth);
		rankingService.resetMonthlyRankings();

		log.info("월간 초기화 완료");
	}

	@Override
	public void retryFailedMessages(String monthYear) {
		if (monthYear == null) {
			monthYear = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
		}
		reviewService.retryFailedMessages(monthYear);
		log.info("실패 메시지 재전송 완료: {}", monthYear);
	}

	@Override
	public Map<String, Object> getSchedulerStatus() {
		Map<String, Object> status = new HashMap<>();

		String monthlyStatus = schedulerRedisRepository.getSchedulerStatus("monthly_reset"); // 변경된 부분
		status.put("monthlyReset", parseStatus(monthlyStatus));

		String retryStatus = schedulerRedisRepository.getSchedulerStatus("retry_message"); // 변경된 부분
		status.put("retryMessage", parseStatus(retryStatus));

		String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
		int failedCount = reviewService.getFailedMessageCount(currentMonth);
		long totalUsers = userRepository.count();

		Map<String, Object> failedInfo = new HashMap<>();
		failedInfo.put("monthYear", currentMonth);
		failedInfo.put("failedCount", failedCount);
		failedInfo.put("totalUsers", totalUsers);
		failedInfo.put("failureRate", totalUsers > 0 ? (double)failedCount / totalUsers * 100 : 0.0);

		status.put("failedMessages", failedInfo);
		status.put("lastChecked", LocalDateTime.now());

		return status;
	}

	@Override
	public Map<String, Object> getFailedMessageStatus(String monthYear) {
		if (monthYear == null) {
			monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
		}

		int failedCount = reviewService.getFailedMessageCount(monthYear);
		long totalUsers = userRepository.count();

		Map<String, Object> result = new HashMap<>();
		result.put("monthYear", monthYear);
		result.put("failedCount", failedCount);
		result.put("totalUsers", totalUsers);
		result.put("failureRate", totalUsers > 0 ? Math.round((double) failedCount / totalUsers * 100 * 100.0) / 100.0 : 0.0);
		result.put("checkedAt", LocalDateTime.now());

		return result;
	}

	private Map<String, Object> parseStatus(String statusString) {
		Map<String, Object> parsed = new HashMap<>();

		if (statusString == null) {
			parsed.put("status", "UNKNOWN");
			parsed.put("message", "실행 기록이 없습니다");
			parsed.put("lastExecuted", null);
			return parsed;
		}

		try {
			String[] parts = statusString.split("\\|");
			if (parts.length >= 3) {
				parsed.put("lastExecuted", parts[0]);
				parsed.put("status", parts[1]);
				parsed.put("message", parts[2]);
			} else {
				parsed.put("status", "ERROR");
				parsed.put("message", "상태 정보 파싱 실패");
				parsed.put("lastExecuted", null);
			}
		} catch (Exception e) {
			parsed.put("status", "ERROR");
			parsed.put("message", "상태 정보 파싱 오류");
			parsed.put("lastExecuted", null);
		}

		return parsed;
	}
}
