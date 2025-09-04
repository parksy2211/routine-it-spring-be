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

	private List<String> achievements;
	private Integer scoreDifference;
	private Integer groupDifference;

	private String messageContent; // 실제 전송된 카카오톡 메시지
	private Boolean messageSent;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;
}