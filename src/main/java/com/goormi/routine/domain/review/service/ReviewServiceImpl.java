package com.goormi.routine.domain.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormi.routine.domain.auth.repository.RedisRepository;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import com.goormi.routine.domain.notification.entity.NotificationType;
import com.goormi.routine.domain.notification.service.NotificationService;
import com.goormi.routine.domain.ranking.service.RankingService;
import com.goormi.routine.domain.review.dto.MonthlyReviewResponse;
import com.goormi.routine.domain.review.dto.UserReviewHistoryResponse;
import com.goormi.routine.domain.review.repository.ReviewRedisRepository;
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
	private final NotificationService notificationService;
	private final GroupMemberRepository groupMemberRepository;
	private final ReviewRedisRepository reviewRedisRepository;
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
					// 실패한 사용자 정보를 Redis에 저장 (재전송용)
					reviewRedisRepository.saveFailedMessage(user.getId(), targetMonth, e.getMessage());
				}
			}

			log.info("월간 회고 메시지 전송 완료: 월 = {}, 성공 = {}, 실패 = {}",
				targetMonth, successCount, failCount);

			if (failCount > 0) {
				throw new RuntimeException(String.format("일부 메시지 전송 실패: 성공 %d건, 실패 %d건", successCount, failCount));
			}

		} catch (Exception e) {
			log.error("월간 회고 메시지 전송 실패: 월 = {}", targetMonth, e);
			throw new RuntimeException("월간 회고 메시지 전송 중 오류가 발생했습니다.", e);
		}
	}


	@Override
	public void sendUserReviewMessage(Long userId, String monthYear) {
		if (userId == null) {
			throw new IllegalArgumentException("사용자 ID는 필수입니다.");
		}

		try {
			MonthlyReviewResponse currentReview = calculateMonthlyReview(userId, monthYear);

			String messageContent = generateReviewMessage(currentReview);
			currentReview.setMessageContent(messageContent);
			currentReview.setMessageSent(true);

			saveReviewToRedis(currentReview);

			notificationService.createNotification(
				NotificationType.MONTHLY_REVIEW,
				null,
				userId,
				null
			);

			log.info("사용자 회고 메시지 전송 완료: 사용자 ID = {}, 월 = {}", userId, monthYear);

		} catch (Exception e) {
			log.error("사용자 회고 메시지 전송 실패: 사용자 ID = {}, 월 = {}", userId, monthYear, e);
			throw new RuntimeException("회고 메시지 전송 실패: " + e.getMessage(), e);
		}
	}


	@Override
	public void retryFailedMessages(String monthYear) {
		String targetMonth = monthYear != null ? monthYear :
			LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

		try {
			List<Long> failedUserIds = reviewRedisRepository.getFailedUserIds(targetMonth);

			if (failedUserIds.isEmpty()) {
				log.info("재전송할 실패 메시지가 없습니다. 월: {}", targetMonth);
				return;
			}

			int retrySuccessCount = 0;
			int retryFailCount = 0;

			for (Long userId : failedUserIds) {
				try {
					sendUserReviewMessage(userId, targetMonth);
					reviewRedisRepository.removeFailedMessage(userId, targetMonth);
					retrySuccessCount++;
				} catch (Exception e) {
					retryFailCount++;
					log.error("회고 메시지 재전송 실패: 사용자 ID = {}", userId, e);
				}
			}

			log.info("회고 메시지 재전송 완료: 월 = {}, 성공 = {}, 실패 = {}",
				targetMonth, retrySuccessCount, retryFailCount);

		} catch (Exception e) {
			log.error("회고 메시지 재전송 중 오류 발생: 월 = {}", targetMonth, e);
			throw new RuntimeException("회고 메시지 재전송 실패: " + e.getMessage(), e);
		}
	}

	@Override
	public int getFailedMessageCount(String monthYear) {
		String targetMonth = monthYear != null ? monthYear :
			LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

		return reviewRedisRepository.getFailedMessageCount(targetMonth);
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
				String previousData = reviewRedisRepository.getReviewData(userId.toString(), previousMonth);
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
				.monthYear(monthYear)
				.totalScore((int)currentScore)
				.participatingGroups(currentGroups)
				.createdAt(LocalDateTime.now())
				.build();

		} catch (Exception e) {
			log.error("월간 회고 계산 실패: 사용자 ID = {}, 월 = {}", userId, monthYear, e);
			throw new RuntimeException("회고 계산 중 오류가 발생했습니다.", e);
		}
	}

	private void saveReviewToRedis(MonthlyReviewResponse review) {
		try {
			String jsonData = objectMapper.writeValueAsString(review);
			reviewRedisRepository.saveReviewData(review.getUserId().toString(), review.getMonthYear(), jsonData); // 변경된 부분
		} catch (JsonProcessingException e) {
			log.error("회고 데이터 JSON 변환 실패: 사용자 ID = {}, 월 = {}",
				review.getUserId(), review.getMonthYear(), e);
			throw new RuntimeException("회고 데이터 저장 실패", e);
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
