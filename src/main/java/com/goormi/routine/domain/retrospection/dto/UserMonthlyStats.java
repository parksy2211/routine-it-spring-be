package com.goormi.routine.domain.retrospection.dto;

import java.util.List;

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
public class UserMonthlyStats {
	private Integer userId;
	private String monthYear;
	private Integer totalScore;
	private Integer groupAuthCount;
	private Integer basicMissionCount;
	private Integer consecutiveDays;
	private Integer participatingGroups;
	private List<GroupScoreDetail> groupScores;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class GroupScoreDetail {
		private Integer groupId;
		private String groupName;
		private String groupType;
		private Integer score;
		private Integer authCount;
		private Integer rankInGroup;
	}
}
