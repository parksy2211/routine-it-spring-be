package com.goormi.routine.domain.scheduler.controller;

import com.goormi.routine.common.response.ApiResponse;
import com.goormi.routine.domain.scheduler.service.MonthlySchedulerService;
import com.goormi.routine.domain.scheduler.service.SchedulerManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "스케줄러", description = "스케줄러 관리 API")
public class SchedulerController {

	private final MonthlySchedulerService monthlySchedulerService;
	private final SchedulerManagementService schedulerManagementService;

	@Operation(
		summary = "수동 월간 초기화 실행",
		description = "월간 랭킹 리셋과 회고 메시지 전송을 수동으로 실행합니다.",
		hidden = true
	)
	@PostMapping("/monthly-reset")
	public ApiResponse<Void> executeManualMonthlyReset() {
		monthlySchedulerService.manualMonthlyReset();
		return ApiResponse.success("수동 월간 초기화가 완료되었습니다.", null);
	}

	@Operation(
		summary = "수동 회고 메시지 재전송",
		description = "실패한 회고 메시지를 수동으로 재전송합니다.",
		hidden = true
	)
	@PostMapping("/retry-messages")
	public ApiResponse<Void> executeManualRetryMessages(
		@Parameter(description = "대상 월 (YYYY-MM), 필수")
		@RequestParam String monthYear) {

		monthlySchedulerService.manualRetryReviewMessages(monthYear);
		return ApiResponse.success("수동 회고 메시지 재전송이 완료되었습니다.", null);
	}
}