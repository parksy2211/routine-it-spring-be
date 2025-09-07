package com.goormi.routine.domain.review.dto;

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
public class MonthlyReviewResponse {
	private Long userId;
	private String nickname;
	private String profileImageUrl;
	private String monthYear;

	private Integer totalScore;
	private Integer totalAuthCount;
	private Integer consecutiveDays;
	private Integer participatingGroups;

	private Integer groupAuthCount;
	private Integer personalRoutineCount;
	private Integer dailyChecklistCount;
	private Integer totalActiveDays;

	private Integer personalRoutineAchievementRate;

	private List<String> achievements;
	private Integer scoreDifference;
	private Integer groupDifference;

	private String messageContent;
	private Boolean messageSent;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
}