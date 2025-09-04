package com.goormi.routine.domain.ranking.service;

import java.util.List;

import com.goormi.routine.domain.ranking.dto.GlobalGroupRankingResponse;
import com.goormi.routine.domain.ranking.dto.GroupTop3RankingResponse;
import com.goormi.routine.domain.ranking.dto.PersonalRankingResponse;

public interface RankingService {

	List<PersonalRankingResponse> getPersonalRankings();

	List<GlobalGroupRankingResponse> getGlobalGroupRankings();

	GroupTop3RankingResponse getTop3RankingsByGroup(Long groupId);

	void updateRankingScore(Long userId, Long groupId, int score);

	void updatePersonalScore(Long userId, int score);

	void updateGroupScore(Long userId, Long groupId, int score);

	void resetMonthlyRankings();

	long getTotalScoreByUser(Long userId);

	void initializeRanking(Long userId, Long groupId);
}
