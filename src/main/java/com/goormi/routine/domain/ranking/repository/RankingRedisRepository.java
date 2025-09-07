package com.goormi.routine.domain.ranking.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import com.goormi.routine.domain.auth.repository.RedisRepository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RankingRedisRepository {

	private final RedisRepository redisRepository;

	private static final String LAST_RESET_MONTH_KEY = "ranking:last_reset_month";

	public void saveLastResetMonth(String monthYear) {
		try {
			redisRepository.saveData(LAST_RESET_MONTH_KEY, monthYear, 0); // 만료 시간 없음
			log.debug("랭킹 마지막 초기화 월 저장: {}", monthYear);
		} catch (Exception e) {
			log.error("랭킹 마지막 초기화 월 저장 실패: {}", monthYear, e);
			throw new RuntimeException("랭킹 초기화 월 저장 실패", e);
		}
	}

	public String getLastResetMonth() {
		try {
			return redisRepository.getData(LAST_RESET_MONTH_KEY);
		} catch (Exception e) {
			log.error("랭킹 마지막 초기화 월 조회 실패", e);
			return null;
		}
	}

	public boolean isResetNeeded(String currentMonth) {
		String lastResetMonth = getLastResetMonth();
		return lastResetMonth == null || !currentMonth.equals(lastResetMonth);
	}
}
