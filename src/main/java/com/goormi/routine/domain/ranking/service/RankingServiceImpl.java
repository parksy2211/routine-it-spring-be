package com.goormi.routine.domain.ranking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goormi.routine.domain.auth.repository.RedisRepository;
import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import com.goormi.routine.domain.ranking.dto.GlobalGroupRankingResponse;
import com.goormi.routine.domain.ranking.dto.GroupTop3RankingResponse;
import com.goormi.routine.domain.ranking.dto.PersonalRankingResponse;
import com.goormi.routine.domain.ranking.entity.Ranking;
import com.goormi.routine.domain.ranking.repository.RankingRedisRepository;
import com.goormi.routine.domain.ranking.repository.RankingRepository;
import com.goormi.routine.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

	private final RankingRepository rankingRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final RankingRedisRepository rankingRedisRepository;

	@Override
	public List<PersonalRankingResponse> getPersonalRankings() {
		try {
			String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
			List<Ranking> personalRankings = rankingRepository.findPersonalRankingsOrderByScore();

			return IntStream.range(0, personalRankings.size())
				.mapToObj(index -> {
					Ranking ranking = personalRankings.get(index);
					User user = ranking.getUser();

					return PersonalRankingResponse.builder()
						.currentRank(index + 1)
						.userId(ranking.getUserId())
						.nickname(user != null ? user.getNickname() : "탈퇴한 사용자")
						.profileImageUrl(user != null ? user.getProfileImageUrl() : null)
						.totalScore(ranking.getScore())
						.totalParticipants(personalRankings.size())
						.monthYear(currentMonthYear)
						.consecutiveDays(0) // TODO: 연속 일수 계산 로직 구현 필요
						.groupDetails(Collections.emptyList()) // TODO: 그룹별 상세 정보 구현 필요
						.updatedAt(ranking.getUpdatedAt())
						.build();
				})
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("개인 랭킹 조회 실패", e);
			throw new RuntimeException("개인 랭킹 조회 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	public List<GlobalGroupRankingResponse> getGlobalGroupRankings() {
		try {
			List<Ranking> groupRankings = rankingRepository.findGroupRankingsOrderByScore();
			String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

			List<GlobalGroupRankingResponse.GroupRankingItem> rankingItems =
				IntStream.range(0, groupRankings.size())
					.mapToObj(index -> {
						Ranking ranking = groupRankings.get(index);
						Group group = ranking.getGroup();
						int rank = index + 1;

						// TODO: 루틴 인증 엔티티 구현 후 수정 필요
						int memberCount = groupMemberRepository.countMembersByGroupId(ranking.getGroupId());
						int activeMembers = 0; // groupMemberRepository.countActiveByGroupId(ranking.getGroupId(), monthYear);
						int totalAuthCount = 0; // groupMemberRepository.countAuthByGroupId(ranking.getGroupId(), monthYear);

						double participationRate = memberCount > 0
							? (double)activeMembers / memberCount : 0.0;

						double averageAuthPerMember = memberCount > 0
							? (double)totalAuthCount / memberCount : 0.0;

						return GlobalGroupRankingResponse.GroupRankingItem.builder()
							.rank(rank)
							.groupId(ranking.getGroupId())
							.groupName(group != null ? group.getGroupName() : "삭제된 그룹")
							.groupImageUrl(group != null ? group.getGroupImageUrl() : null)
							.category(group != null ? group.getCategory() : null)
							.groupType(group != null ? group.getGroupType().name() : null)
							.totalScore(ranking.getScore())
							.memberCount(memberCount)
							.activeMembers(activeMembers)
							.participationRate(Math.round(participationRate * 100.0) / 100.0)
							.totalAuthCount(totalAuthCount)
							.averageAuthPerMember(Math.round(averageAuthPerMember * 100.0) / 100.0)
							.build();
					})
					.collect(Collectors.toList());

			GlobalGroupRankingResponse response = GlobalGroupRankingResponse.builder()
				.rankings(rankingItems)
				.monthYear(monthYear)
				.totalGroups(groupRankings.size())
				.updatedAt(LocalDateTime.now())
				.build();

			return Collections.singletonList(response);
		} catch (Exception e) {
			log.error("그룹 랭킹 조회 실패", e);
			throw new RuntimeException("그룹 랭킹 조회 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	public GroupTop3RankingResponse getTop3RankingsByGroup(Long groupId) {
		if (groupId == null) {
			throw new IllegalArgumentException("그룹 ID는 필수입니다.");
		}

		try {
			List<Ranking> top3Rankings = rankingRepository.findTop3UsersByGroupId(groupId);
			String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

			if (top3Rankings.isEmpty()) {
				return GroupTop3RankingResponse.builder()
					.groupId(groupId)
					.top3Users(Collections.emptyList())
					.totalMembers(0)
					.monthYear(currentMonthYear)
					.updatedAt(LocalDateTime.now())
					.build();
			}

			Group group = top3Rankings.get(0).getGroup();
			double groupWeightMultiplier = getGroupWeightMultiplier(group);

			List<GroupTop3RankingResponse.UserRankingItem> userRankingItems =
				IntStream.range(0, top3Rankings.size())
					.mapToObj(index -> {
						Ranking ranking = top3Rankings.get(index);
						User user = ranking.getUser();

						// TODO: 실제 인증 횟수, 연속 일수, 점수 세부사항 계산 로직 구현 필요
						int authCount = 0;
						int consecutiveDays = 0;
						double consecutiveBonus = calculateConsecutiveBonus(consecutiveDays);

						GroupTop3RankingResponse.ScoreBreakdown scoreBreakdown =
							GroupTop3RankingResponse.ScoreBreakdown.builder()
								.baseScore(authCount * 10)
								.weightMultiplier(groupWeightMultiplier)
								.weightedScore((int)(authCount * 10 * groupWeightMultiplier))
								.consecutiveBonus(consecutiveBonus)
								.finalScore(ranking.getScore())
								.build();

						return GroupTop3RankingResponse.UserRankingItem.builder()
							.rank(index + 1)
							.userId(ranking.getUserId())
							.nickname(user != null ? user.getNickname() : "탈퇴한 사용자")
							.profileImageUrl(user != null ? user.getProfileImageUrl() : null)
							.score(ranking.getScore())
							.authCount(authCount)
							.consecutiveDays(consecutiveDays)
							.consecutiveBonus(consecutiveBonus)
							.scoreBreakdown(scoreBreakdown)
							.build();
					})
					.collect(Collectors.toList());

			return GroupTop3RankingResponse.builder()
				.groupId(groupId)
				.groupName(group != null ? group.getGroupName() : "삭제된 그룹")
				.groupType(group != null ? group.getGroupType().name() : null)
				.groupWeightMultiplier(groupWeightMultiplier)
				.monthYear(currentMonthYear)
				.top3Users(userRankingItems)
				.totalMembers(groupMemberRepository.countMembersByGroupId(groupId))
				.updatedAt(LocalDateTime.now())
				.build();
		} catch (Exception e) {
			log.error("그룹 Top3 랭킹 조회 실패: 그룹 ID = {}", groupId, e);
			throw new RuntimeException("그룹 Top3 랭킹 조회 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	@Transactional
	public void updateRankingScore(Long userId, Long groupId, int score) {
		if (userId == null) {
			throw new IllegalArgumentException("사용자 ID는 필수입니다.");
		}
		if (score < 0) {
			throw new IllegalArgumentException("점수는 0 이상이어야 합니다.");
		}

		try {
			if (groupId != null) {
				updateGroupScore(userId, groupId, score);
			} else {
				updatePersonalScore(userId, score);
			}
		} catch (Exception e) {
			log.error("랭킹 점수 업데이트 실패: 사용자 ID = {}, 그룹 ID = {}, 점수 = {}", userId, groupId, score, e);
			throw new RuntimeException("랭킹 점수 업데이트 중 오류가 발생했습니다.", e);

		}
	}

	@Override
	@Transactional
	public void updatePersonalScore(Long userId, int score) {
		try {
			Optional<Ranking> existingRanking = rankingRepository.findByUserIdAndGroupId(userId, null);

			if (existingRanking.isPresent()) {
				Ranking ranking = existingRanking.get();
				ranking.setScore(ranking.getScore() + score);
				ranking.setUpdatedAt(LocalDateTime.now());
				rankingRepository.save(ranking);
				log.info("개인 점수 업데이트: 사용자 ID = {}, 추가 점수 = {}, 총 점수 = {}",
					userId, score, ranking.getScore());
			} else {
				initializeRanking(userId, null);
				updatePersonalScore(userId, score);
			}
		} catch (Exception e) {
			log.error("개인 점수 업데이트 실패: 사용자 ID = {}, 점수 = {}", userId, score, e);
			throw new RuntimeException("개인 점수 업데이트 실패", e);
		}
	}

	@Override
	@Transactional
	public void updateGroupScore(Long userId, Long groupId, int score) {
		try {
			Optional<Ranking> existingRanking = rankingRepository.findByUserIdAndGroupId(userId, groupId);

			if (existingRanking.isPresent()) {
				Ranking ranking = existingRanking.get();
				ranking.setScore(ranking.getScore() + score);
				ranking.setUpdatedAt(LocalDateTime.now());
				rankingRepository.save(ranking);
				log.info("그룹 점수 업데이트: 사용자 ID = {}, 그룹 ID = {}, 추가 점수 = {}, 총 점수 = {}",
					userId, groupId, score, ranking.getScore());
			} else {
				initializeRanking(userId, groupId);
				updateGroupScore(userId, groupId, score);
			}
		} catch (Exception e) {
			log.error("그룹 점수 업데이트 실패: 사용자 ID = {}, 그룹 ID = {}, 점수 = {}", userId, groupId, score, e);
			throw new RuntimeException("그룹 점수 업데이트 실패", e);
		}
	}

	@Override
	@Transactional
	public void resetMonthlyRankings() {
		String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

		List<Ranking> allRankings = rankingRepository.findAll();
		if (allRankings.isEmpty()) {
			log.warn("리셋할 랭킹 데이터가 없습니다.");
			return;
		}

		try {
			for (Ranking ranking : allRankings) {
				ranking.setScore(0);
				ranking.setMonthYear(currentMonth);
				ranking.setUpdatedAt(LocalDateTime.now());
			}

			rankingRepository.saveAll(allRankings);
			rankingRedisRepository.saveLastResetMonth(currentMonth);

			log.info("월별 랭킹 리셋 완료: 총 {} 개의 랭킹이 리셋되었습니다", allRankings.size());

		} catch (Exception e) {
			log.error("월별 랭킹 리셋 중 오류 발생", e);
			throw new RuntimeException("월별 랭킹 리셋 실패", e);
		}
	}

	@Override
	public long getTotalScoreByUser(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("사용자 ID는 필수입니다.");
		}

		try {
			return rankingRepository.getTotalScoreByUserId(userId).orElse(0L);
		} catch (Exception e) {
			log.error("사용자 총 점수 조회 실패: 사용자 ID = {}", userId, e);
			throw new RuntimeException("사용자 총 점수 조회 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	@Transactional
	public void initializeRanking(Long userId, Long groupId) {
		if (userId == null) {
			throw new IllegalArgumentException("사용자 ID는 필수입니다.");
		}

		try {
			String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
			Optional<Ranking> existingRanking = rankingRepository.findByUserIdAndGroupId(userId, groupId);

			if (existingRanking.isEmpty()) {
				Long rankingId = System.currentTimeMillis();

				Ranking newRanking = Ranking.builder()
					.rankingId(rankingId)
					.userId(userId)
					.groupId(groupId)
					.score(0)
					.monthYear(currentMonthYear)
					.updatedAt(LocalDateTime.now())
					.build();

				rankingRepository.save(newRanking);
				log.info("새로운 랭킹 초기화: 사용자 ID = {}, 그룹 ID = {}, 월 = {}",
					userId, groupId, currentMonthYear);
			}
		} catch (Exception e) {
			log.error("랭킹 초기화 실패: 사용자 ID = {}, 그룹 ID = {}", userId, groupId, e);
			throw new RuntimeException("랭킹 초기화 중 오류가 발생했습니다.", e);
		}
	}

	private double getGroupWeightMultiplier(Group group) {
		if (group == null || group.getGroupType() == null) {
			return 1.0;
		}

		return switch (group.getGroupType()) {
			case REQUIRED -> // 의무 참여
				1.5;
			case FREE -> // 자유 참여
				1.2;
			default -> 1.0;
		};
	}

	private double calculateConsecutiveBonus(int consecutiveDays) {
		if (consecutiveDays <= 2 && consecutiveDays < 30) {
			return consecutiveDays * 0.5;
		} else if (consecutiveDays >= 30) {
			return 15;
		}
		return 0;
	}
}