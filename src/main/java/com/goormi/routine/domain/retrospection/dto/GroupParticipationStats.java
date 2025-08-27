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
public class GroupParticipationStats {
	private Long groupId;
	private String groupName;
	private String groupType;
	private String monthYear;
	private Integer totalMembers;
	private Integer activeMembers;
	private Double participationRate;
	private Integer totalAuthCount;
	private Double averageAuthPerMember;
	private Integer highestScore;
	private Integer lowestScore;
	private Double averageScore;
}

