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
public class GlobalGroupRankingResponse {
	private List<GroupRankingItem> rankings;
	private String monthYear;
	private Integer totalGroups;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class GroupRankingItem {
		private Integer rank;
		private Long groupId;
		private String groupName;
		private String groupImageUrl;
		private String category;
		private String groupType;
		private Integer totalScore;
		private Integer memberCount;
		private Integer activeMembers;
		private Double participationRate; // 참여율 (activeMembers / memberCount)
		private Integer totalAuthCount;
		private Double averageAuthPerMember;
	}
}