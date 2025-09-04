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
public class PersonalRankingRequest {
	private String monthYear; // 선택적, 없으면 현재 월
	private Integer page;
	private Integer size;
}
