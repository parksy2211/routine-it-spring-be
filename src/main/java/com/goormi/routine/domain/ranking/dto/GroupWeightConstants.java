package com.goormi.routine.domain.ranking.dto;

public class GroupWeightConstants {
	public static final Double FREE_GROUP_WEIGHT = 1.2;
	public static final Double REQUIRED_GROUP_WEIGHT = 1.5;
	public static final Integer BASE_SCORE_PER_AUTH = 10;
	public static final Double CONSECUTIVE_BONUS_MULTIPLIER = 0.5;
	public static final Integer MAX_CONSECUTIVE_DAYS_FOR_BONUS = 30;

	public static Double getWeightMultiplier(String groupType) {
		return "REQUIRED".equals(groupType) ? REQUIRED_GROUP_WEIGHT : FREE_GROUP_WEIGHT;
	}
}
