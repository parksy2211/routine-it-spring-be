package com.goormi.routine.domain.scheduler.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import com.goormi.routine.domain.auth.repository.RedisRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SchedulerRedisRepository {

	private final RedisRepository redisRepository;

	private static final String SCHEDULER_STATUS_PREFIX = "scheduler:status:";
	private static final int STATUS_EXPIRE_DAYS = 7;

	public void updateSchedulerStatus(String schedulerName, String status, String message) {
		try {
			String key = SCHEDULER_STATUS_PREFIX + schedulerName;
			String value = String.format("%s|%s|%s", LocalDateTime.now(), status, message);
			redisRepository.saveData(key, value, STATUS_EXPIRE_DAYS * 24 * 60 * 60);
			log.debug("스케줄러 상태 업데이트: {} = {}", schedulerName, status);
		} catch (Exception e) {
			log.error("스케줄러 상태 업데이트 실패: {}", schedulerName, e);
		}
	}

	public String getSchedulerStatus(String schedulerName) {
		try {
			String key = SCHEDULER_STATUS_PREFIX + schedulerName;
			return redisRepository.getData(key);
		} catch (Exception e) {
			log.error("스케줄러 상태 조회 실패: {}", schedulerName, e);
			return null;
		}
	}

	public Map<String, String> getAllSchedulerStatus() {
		try {
			String pattern = SCHEDULER_STATUS_PREFIX + "*";
			Map<String, String> statusMap = new HashMap<>();

			redisRepository.getKeysByPattern(pattern).forEach(key -> {
				String schedulerName = key.replace(SCHEDULER_STATUS_PREFIX, "");
				String status = redisRepository.getData(key);
				statusMap.put(schedulerName, status);
			});

			return statusMap;
		} catch (Exception e) {
			log.error("전체 스케줄러 상태 조회 실패", e);
			return new HashMap<>();
		}
	}

	public boolean isSchedulerHealthy(String schedulerName) {
		String status = getSchedulerStatus(schedulerName);
		return status == null || !status.contains("FAILED");
	}
}
