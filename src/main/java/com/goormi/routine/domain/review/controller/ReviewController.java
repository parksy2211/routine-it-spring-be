package com.goormi.routine.domain.review.controller;

import com.goormi.routine.domain.review.service.ReviewService;
import com.goormi.routine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review", description = "월간 회고 시스템 API")
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(
		summary = "월간 회고 메시지 전송",
		description = "매월 1일 사용자에게 월간 회고 메시지를 전달합니다. " +
			"전달된 회고 데이터는 Redis에만 저장되며, 실제 메시지는 카카오톡에서 확인 가능합니다."
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회고 메시지 전송 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "메시지 전송 실패")
	})
	@PostMapping("/reviews/monthly")
	public ResponseEntity<ApiResponse<String>> sendMonthlyReviewMessages(
		@Parameter(description = "대상 월 (YYYY-MM), 미입력시 이전 월")
		@RequestParam(required = false) String monthYear,
		@Parameter(description = "특정 사용자들에게만 전송 (미입력시 전체 발송)")
		@RequestParam(required = false) List<Long> userIds) {

		try {
			String targetMonth = monthYear != null ? monthYear :
				LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

			log.info("월간 회고 메시지 전송 요청: 월 = {}, 대상 사용자 수 = {}",
				targetMonth, userIds != null ? userIds.size() : "전체");

			if (userIds != null && !userIds.isEmpty()) {
				int successCount = 0;
				for (Long userId : userIds) {
					try {
						reviewService.sendUserReviewMessage(userId, targetMonth);
						successCount++;
					} catch (Exception e) {
						log.error("개별 사용자 회고 전송 실패: ID = {}", userId, e);
					}
				}
				return ResponseEntity.ok(ApiResponse.success(
					String.format("선택된 사용자 %d명 중 %d명에게 회고 메시지가 전송되었습니다.",
						userIds.size(), successCount),
					"전송 완료"
				));
			} else {
				reviewService.sendMonthlyReviewMessages(targetMonth);
				return ResponseEntity.ok(ApiResponse.success(
					"전체 사용자에게 월간 회고 메시지가 전송되었습니다.",
					"전송 완료"
				));
			}

		} catch (Exception e) {
			log.error("월간 회고 메시지 전송 실패", e);
			return ResponseEntity.internalServerError().body(ApiResponse.error(
				"월간 회고 메시지 전송 중 오류가 발생했습니다: " + e.getMessage()
			));
		}
	}
}