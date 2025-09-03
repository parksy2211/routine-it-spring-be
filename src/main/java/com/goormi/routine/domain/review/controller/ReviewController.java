package com.goormi.routine.domain.review.controller;

import com.goormi.routine.domain.review.service.ReviewService;
import com.goormi.routine.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
		description = "매월 1일 사용자에게 월간 회고 메시지를 전달합니다. "
	)
	@PostMapping("/reviews/monthly")
	public ApiResponse<String> sendMonthlyReviewMessages(
		@Parameter(description = "대상 월 (YYYY-MM), 미입력시 이전 월")
		@RequestParam(required = false) String monthYear,
		@Parameter(description = "특정 사용자들에게만 전송 (미입력시 전체 발송)")
		@RequestParam(required = false) List<Long> userIds) {
		if (userIds != null && !userIds.isEmpty()) {
			// 개별 사용자 전송 처리는 서비스에서 담당
			for (Long userId : userIds) {
				reviewService.sendUserReviewMessage(userId, monthYear);
			}
			return ApiResponse.success("선택된 사용자들에게 회고 메시지가 전송되었습니다.");
		} else {
			reviewService.sendMonthlyReviewMessages(monthYear);
			return ApiResponse.success("전체 사용자에게 월간 회고 메시지가 전송되었습니다.");
		}
	}
}