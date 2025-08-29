package com.goormi.routine.domain.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormi.routine.domain.auth.repository.RedisRepository;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import com.goormi.routine.domain.ranking.service.RankingService;
import com.goormi.routine.domain.review.dto.MonthlyReviewResponse;
import com.goormi.routine.domain.review.dto.UserReviewHistoryResponse;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService{

	private final UserRepository userRepository;
	private final RankingService rankingService;
	private final GroupMemberRepository groupMemberRepository;
	private final RedisRepository redisRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void sendMonthlyReviewMessages(String monthYear) {
		String targetMonth = monthYear != null ? monthYear :
			LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

		try {
			List<User> activeUsers = userRepository.findAll();

			int successCount = 0;
			int failCount = 0;

			for (User user : activeUsers) {
				try {
					sendUserReviewMessage(user.getId(), targetMonth);
					successCount++;
				} catch (Exception e) {
					failCount++;
					log.error("사용자 회고 메시지 전송 실패: 사용자 ID = {}", user.getId(), e);
				}
			}

			log.info("월간 회고 메시지 전송 완료: 월 = {}, 성공 = {}, 실패 = {}",
				targetMonth, successCount, failCount);

		} catch (Exception e) {
			log.error("월간 회고 메시지 전송 중 오류 발생: 월 = {}", targetMonth, e);
		}
	}

	@Override
	public void sendUserReviewMessage(Long userId, String monthYear) {
		try {
			MonthlyReviewResponse currentReview = calculateMonthlyReview(userId, monthYear);

			String messageContent = generateReviewMessage(currentReview);
			currentReview.setMessageContent(messageContent);
			currentReview.setMessageSent(true);

			saveReviewToRedis(currentReview);

			// TODO: 실제 카카오 메시지 전송
			// kakaoMessageService.sendMessage(user.getKakaoId(), messageContent);

			log.info("사용자 회고 메시지 전송 완료: 사용자 ID = {}, 월 = {}", userId, monthYear);

		} catch (Exception e) {
			log.error("사용자 회고 메시지 전송 실패: 사용자 ID = {}, 월 = {}", userId, monthYear, e);
			throw new RuntimeException("회고 메시지 전송 실패", e);
		}
	}

	@Override
	public MonthlyReviewResponse getUserReview(Long userId, String monthYear) {
		try {
			String reviewData = redisRepository.getReviewData(userId.toString(), monthYear);

			if (reviewData != null) {
				return parseReviewData(reviewData);
			} else {
				return calculateMonthlyReview(userId, monthYear);
			}

		} catch (Exception e) {
			log.error("회고 기록 조회 실패: 사용자 ID = {}, 월 = {}", userId, monthYear, e);
			return null;
		}
	}

	@Override
	public UserReviewHistoryResponse getUserReviewHistory(Long userId) {
		try {
			List<String> reviewKeys = redisRepository.getUserReviewKeys(userId.toString());

			List<UserReviewHistoryResponse.MonthlyReviewSummary> summaries = new ArrayList<>();

			for (String key : reviewKeys) {
				String monthYear = key.substring(key.lastIndexOf(":") + 1);

				try {
					String reviewData = redisRepository.getReviewData(userId.toString(), monthYear);
					if (reviewData != null) {
						MonthlyReviewResponse review = parseReviewData(reviewData);

						UserReviewHistoryResponse.MonthlyReviewSummary summary =
							UserReviewHistoryResponse.MonthlyReviewSummary.builder()
								.monthYear(review.getMonthYear())
								.totalScore(review.getTotalScore())
								.participatingGroups(review.getParticipatingGroups())
								.scoreDifference(review.getScoreDifference())
								.messageSent(review.getMessageSent())
								.createdAt(review.getCreatedAt())
								.build();

						summaries.add(summary);
					}
				} catch (Exception e) {
					log.warn("회고 데이터 파싱 실패: 사용자 ID = {}, 월 = {}", userId, monthYear, e);
				}
			}

			summaries.sort((a, b) -> b.getMonthYear().compareTo(a.getMonthYear()));

			return UserReviewHistoryResponse.builder()
				.reviews(summaries)
				.totalCount(summaries.size())
				.updatedAt(LocalDateTime.now())
				.build();

		} catch (Exception e) {
			log.error("회고 히스토리 조회 실패: 사용자 ID = {}", userId, e);
			return UserReviewHistoryResponse.builder()
				.reviews(new ArrayList<>())
				.totalCount(0)
				.updatedAt(LocalDateTime.now())
				.build();
		}
	}

	private MonthlyReviewResponse calculateMonthlyReview(Long userId, String monthYear) {
		try {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

			long currentScore = rankingService.getTotalScoreByUser(userId);
			int currentGroups = groupMemberRepository.findActiveGroupsByUserId(userId).size();

			String previousMonth = getPreviousMonth(monthYear);
			MonthlyReviewResponse previousReview = null;
			if (previousMonth != null) {
				String previousData = redisRepository.getReviewData(userId.toString(), previousMonth);
				if (previousData != null) {
					previousReview = parseReviewData(previousData);
				}
			}

			int scoreDifference = 0;
			int groupDifference = 0;
			List<String> achievements = new ArrayList<>();

			if (previousReview != null) {
				scoreDifference = (int)currentScore - previousReview.getTotalScore();
				groupDifference = currentGroups - previousReview.getParticipatingGroups();

				if (scoreDifference > 0) {
					achievements.add(String.format("지난 달보다 %d점 향상! (%d → %d)",
						scoreDifference, previousReview.getTotalScore(), currentScore));
				}
				if (groupDifference > 0) {
					achievements.add(String.format("새로운 그룹 %d개 참여로 도전 영역 확장!", groupDifference));
				}
			} else {
				achievements.add("루틴잇 첫 달 도전 완료! 🎉");
				if (currentScore > 0) {
					achievements.add(String.format("첫 달 %d점 달성!", currentScore));
				}
			}

			return MonthlyReviewResponse.builder()
				.userId(userId)
				.nickname(user.getNickname())
				.profileImageUrl(user.getProfileImageUrl())
				.monthYear(monthYear)
				.totalScore((int)currentScore)
				.totalAuthCount(0) // 실제 인증 횟수 계산
				.consecutiveDays(0) // 연속 일수 계산
				.participatingGroups(currentGroups)
				.achievements(achievements)
				.scoreDifference(scoreDifference)
				.groupDifference(groupDifference)
				.messageSent(false)
				.createdAt(LocalDateTime.now())
				.build();

		} catch (Exception e) {
			log.error("월간 회고 계산 실패: 사용자 ID = {}, 월 = {}", userId, monthYear, e);
			throw new RuntimeException("회고 계산 실패", e);
		}

	}

	private void saveReviewToRedis(MonthlyReviewResponse review) {
		try {
			String jsonData = objectMapper.writeValueAsString(review);
			redisRepository.saveReviewData(review.getUserId().toString(), review.getMonthYear(), jsonData);

		} catch (JsonProcessingException e) {
			log.error("회고 데이터 JSON 변환 실패: 사용자 ID = {}, 월 = {}",
				review.getUserId(), review.getMonthYear(), e);
		}
	}

	private MonthlyReviewResponse parseReviewData(String jsonData) {
		try {
			return objectMapper.readValue(jsonData, MonthlyReviewResponse.class);
		} catch (JsonProcessingException e) {
			log.error("회고 데이터 JSON 파싱 실패", e);
			return null;
		}
	}

	private String getPreviousMonth(String monthYear) {
		try {
			LocalDate date = LocalDate.parse(monthYear + "-01");
			return date.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
		} catch (Exception e) {
			return null;
		}
	}



	private String generateReviewMessage(MonthlyReviewResponse review) {
		StringBuilder message = new StringBuilder();

		message.append("🎊 ").append(review.getMonthYear()).append(" 월간 루틴 성과 리포트 🎊\n\n");
		message.append("안녕하세요, ").append(review.getNickname()).append("님!\n");
		message.append("이번 달 루틴 성과를 확인해보세요.\n\n");

		message.append("📈 이번 달 성과\n");
		message.append("• 총 점수: ").append(review.getTotalScore()).append("점");

		if (review.getScoreDifference() != null) {
			if (review.getScoreDifference() > 0) {
				message.append(" (📈 +").append(review.getScoreDifference()).append("점 상승!)");
			} else if (review.getScoreDifference() < 0) {
				message.append(" (📉 ").append(review.getScoreDifference()).append("점 하락)");
			} else {
				message.append(" (➡️ 동일)");
			}
		}
		message.append("\n");

		message.append("• 참여 그룹: ").append(review.getParticipatingGroups()).append("개");
		if (review.getGroupDifference() != null && review.getGroupDifference() > 0) {
			message.append(" (👥 +").append(review.getGroupDifference()).append("개 그룹 추가!)");
		}
		message.append("\n");

		message.append("• 총 인증: ").append(review.getTotalAuthCount()).append("회\n");
		message.append("• 연속 출석: ").append(review.getConsecutiveDays()).append("일\n\n");

		if (review.getAchievements() != null && !review.getAchievements().isEmpty()) {
			message.append("🏆 이번 달 성취\n");
			for (String achievement : review.getAchievements()) {
				message.append("✨ ").append(achievement).append("\n");
			}
			message.append("\n");
		}

		if (review.getScoreDifference() != null && review.getScoreDifference() > 0) {
			message.append("💪 지난 달보다 더 발전한 모습이 보여요! 이 기세로 쭉~\n\n");
		} else {
			message.append("💪 꾸준함이 최고의 재능입니다! 다음 달도 화이팅!\n\n");
		}

		message.append("새로운 달에도 함께 성장해요! 🌱\n");
		message.append("루틴잇에서 확인하기 👆");

		return message.toString();
	}
}
