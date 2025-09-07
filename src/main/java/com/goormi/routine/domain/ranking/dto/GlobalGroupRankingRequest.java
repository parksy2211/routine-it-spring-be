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
public class GlobalGroupRankingRequest {
	private String monthYear;
	private String category;
	private String groupType;
	private Integer page;
	private Integer size;
}
