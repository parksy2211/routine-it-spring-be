package com.goormi.routine.domain.ranking.dto;

import java.time.LocalDateTime;

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
public class RankingResetResponse {
	private Boolean success;
	private String message;
	private String resetMonthYear;
	private String newMonthYear;
	private Integer affectedUsers;
	private Integer affectedGroups;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime resetAt;
}