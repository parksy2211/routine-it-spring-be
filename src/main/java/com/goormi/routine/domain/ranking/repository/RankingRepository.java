package com.goormi.routine.domain.ranking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.goormi.routine.domain.ranking.entity.Ranking;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

	Optional<Ranking> findByUserId(Long userId);

	Optional<Ranking> findByGroupId(Long groupId);

	@Query("SELECT r FROM Ranking r WHERE r.userId IS NOT NULL ORDER BY r.personalScore DESC")
	List<Ranking> findPersonalRankingsOrderByScore();

	@Query("SELECT r FROM Ranking r WHERE r.groupId IS NOT NULL ORDER BY r.groupScore DESC")
	List<Ranking> findGroupRankingsOrderByScore();

	@Query("SELECT r FROM Ranking r WHERE r.groupId = :groupId AND r.userId IS NOT NULL ORDER BY r.personalScore DESC LIMIT 3")
	List<Ranking> findTop3UsersByGroupId(@Param("groupId") Long groupId);

	@Query("SELECT r FROM Ranking r WHERE r.groupId = :groupId AND r.userId IS NOT NULL ORDER BY r.personalScore DESC")
	List<Ranking> findAllUsersByGroupIdOrderByScore(@Param("groupId") Long groupId);

	@Query("UPDATE Ranking r SET r.personalScore = 0 WHERE r.userId IS NOT NULL")
	void resetPersonalScores();

	@Query("UPDATE Ranking r SET r.groupScore = 0 WHERE r.groupId IS NOT NULL")
	void resetGroupScores();

	Optional<Ranking> findByUserIdAndGroupId(Long userId, Long groupId);

	@Query("SELECT SUM(r.personalScore) FROM Ranking r WHERE r.userId = :userId")
	Optional<Long> getTotalScoreByUserId(@Param("userId") Long userId);
}