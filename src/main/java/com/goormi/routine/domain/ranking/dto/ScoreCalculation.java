package com.goormi.routine.domain.ranking.dto;

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
public class ScoreCalculation {
	private Long userId;
	private Long groupId;
	private String monthYear;
	private String groupType;

	private Integer baseScorePerAuth; // 기본 점수 (10점)
	private Integer authCount;
	private Integer baseScore;
	private Double weightMultiplier; // 자유:1.2, 의무:1.5
	private Integer weightedScore;

	private Integer consecutiveDays;
	private Integer consecutiveDaysForBonus; // 상한 30일
	private Double consecutiveBonusMultiplier; // 0.5
	private Integer consecutiveBonus; // 상한 15점

	private Integer finalScore;
	private String calculationDetails; // 계산 상세 내역 (디버깅용)

	public void calculateScore() {
		this.baseScore = this.baseScorePerAuth * this.authCount;
		this.weightedScore = (int) Math.round(this.baseScore * this.weightMultiplier);

		this.consecutiveDaysForBonus = Math.min(30, this.consecutiveDays);
		this.consecutiveBonus = (int) (this.consecutiveDaysForBonus * this.consecutiveBonusMultiplier);

		this.finalScore = this.weightedScore + this.consecutiveBonus;

		this.calculationDetails = String.format(
			"기본점수(%d*%d=%d) × 가중치(%.1f)=%d + 연속보너스(min(%d,30)*0.5=%d) = %d",
			this.baseScorePerAuth, this.authCount, this.baseScore,
			this.weightMultiplier, this.weightedScore,
			this.consecutiveDays, this.consecutiveBonus, this.finalScore
		);
	}
}
