package com.goormi.routine.domain.ranking.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTop3RankingResponse {
	private Long groupId;
	private String groupName;
	private String groupType;
	private Double groupWeightMultiplier;
	private String monthYear;
	private List<UserRankingItem> top3Users;
	private Integer totalMembers;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserRankingItem {
		private Integer rank;
		private Long userId;
		private String nickname;
		private String profileImageUrl;
		private Integer score;
		private Integer authCount;
		private Integer consecutiveDays;
		private Integer consecutiveBonus;
		private ScoreBreakdown scoreBreakdown;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ScoreBreakdown {
		private Integer baseScore; // 기본 점수 (인증횟수 * 10)
		private Double weightMultiplier;
		private Integer weightedScore;
		private Integer consecutiveBonus;
		private Integer finalScore;
	}
}
