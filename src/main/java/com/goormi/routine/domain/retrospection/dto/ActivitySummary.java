package com.goormi.routine.domain.retrospection.dto;

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
public class ActivitySummary {
	private Long userId;
	private Long groupId;
	private String monthYear;
	private Integer groupAuthCount;
	private Integer consecutiveDays;
	private String groupType;
}
