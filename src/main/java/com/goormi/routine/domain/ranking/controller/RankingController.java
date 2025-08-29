package com.goormi.routine.domain.ranking.controller;

import com.goormi.routine.domain.auth.annotation.CurrentUser;
import com.goormi.routine.domain.ranking.dto.GlobalGroupRankingRequest;
import com.goormi.routine.domain.ranking.dto.GlobalGroupRankingResponse;
import com.goormi.routine.domain.ranking.dto.GroupTop3RankingRequest;
import com.goormi.routine.domain.ranking.dto.GroupTop3RankingResponse;
import com.goormi.routine.domain.ranking.dto.PersonalRankingRequest;
import com.goormi.routine.domain.ranking.dto.PersonalRankingResponse;
import com.goormi.routine.domain.ranking.dto.RankingResetResponse;
import com.goormi.routine.domain.ranking.service.RankingService;
import com.goormi.routine.domain.user.entity.User;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ranking", description = "랭킹 시스템 API")
public class RankingController {

	private final RankingService rankingService;

	@Operation(
		summary = "개인별 랭킹 조회",
		description = "사용자의 전체 그룹 활동 점수를 합산한 개인 랭킹을 조회합니다. " +
			"새로운 월이 되면 자동으로 랭킹이 초기화됩니다."
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "개인 랭킹 조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@GetMapping("/personal")
	public ResponseEntity<ApiResponse<List<PersonalRankingResponse>>> getPersonalRankings(
		@Parameter(description = "조회할 월 (YYYY-MM 형식), 미입력시 현재 월")
		@RequestParam(required = false) String monthYear,
		@Parameter(description = "페이지 번호 (0부터 시작)")
		@RequestParam(defaultValue = "0") Integer page,
		@Parameter(description = "페이지 크기")
		@RequestParam(defaultValue = "10") Integer size,
		@CurrentUser User currentUser) {

		try {
			String targetMonth = monthYear != null ? monthYear :
				LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

			PersonalRankingRequest request = PersonalRankingRequest.builder()
				.monthYear(targetMonth)
				.page(page)
				.size(size)
				.build();

			List<PersonalRankingResponse> rankings = rankingService.getPersonalRankings();

			return ResponseEntity.ok(ApiResponse.success(
				"개인 랭킹 조회가 완료되었습니다.",
				rankings
			));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(
				"개인 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
			));
		}
	}

	@Operation(
		summary = "그룹별 랭킹 조회",
		description = "그룹의 점수를 합산하여 전체 그룹 랭킹을 조회합니다. " + "새로운 월이 되면 자동으로 랭킹이 초기화됩니다."
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "그룹 랭킹 조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
	})
	@GetMapping("/groups/global")
	public ResponseEntity<ApiResponse<List<GlobalGroupRankingResponse>>> getGlobalGroupRankings(
		@Parameter(description = "조회할 월 (YYYY-MM 형식), 미입력시 현재 월")
		@RequestParam(required = false) String monthYear,
		@Parameter(description = "그룹 카테고리 필터 (운동, 독서, 취미 등)")
		@RequestParam(required = false) String category,
		@Parameter(description = "그룹 타입 필터 (OPTIONAL: 자유참여, MANDATORY: 의무참여)")
		@RequestParam(required = false) String groupType,
		@Parameter(description = "페이지 번호 (0부터 시작)")
		@RequestParam(defaultValue = "0") Integer page,
		@Parameter(description = "페이지 크기")
		@RequestParam(defaultValue = "20") Integer size) {

		try {
			String targetMonth = monthYear != null ? monthYear :
				LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

			GlobalGroupRankingRequest request = GlobalGroupRankingRequest.builder()
				.monthYear(targetMonth)
				.category(category)
				.groupType(groupType)
				.page(page)
				.size(size)
				.build();

			List<GlobalGroupRankingResponse> rankings = rankingService.getGlobalGroupRankings();

			return ResponseEntity.ok(ApiResponse.success(
				"그룹 랭킹 조회가 완료되었습니다.",
				rankings
			));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(
				"그룹 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
			));
		}
	}

	@Operation(
		summary = "그룹 랭킹 상위 3명 조회",
		description = "특정 그룹 내 상위 3명의 랭킹을 조회합니다. " + "새로운 월이 되면 자동으로 랭킹이 초기화됩니다."
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "그룹 Top3 랭킹 조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 그룹 ID"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "그룹 접근 권한 없음"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
	})
	@GetMapping("/groups/{groupId}/top3")
	public ResponseEntity<ApiResponse<GroupTop3RankingResponse>> getGroupTop3Rankings(
		@Parameter(description = "그룹 ID", required = true)
		@PathVariable Long groupId,
		@Parameter(description = "조회할 월 (YYYY-MM 형식), 미입력시 현재 월")
		@RequestParam(required = false) String monthYear,
		@CurrentUser User currentUser) {

		try {
			String targetMonth = monthYear != null ? monthYear :
				LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

			GroupTop3RankingRequest request = GroupTop3RankingRequest.builder()
				.monthYear(targetMonth)
				.build();

			GroupTop3RankingResponse response = rankingService.getTop3RankingsByGroup(groupId);

			return ResponseEntity.ok(ApiResponse.success(
				"그룹 Top3 랭킹 조회가 완료되었습니다.",
				response
			));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(
				"잘못된 그룹 ID입니다: " + e.getMessage()
			));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(
				"그룹 Top3 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
			));
		}
	}

	@Operation(
		summary = "랭킹 초기화",
		description = "매월 1일에 랭킹 점수를 초기화합니다. ",
		hidden = true
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "랭킹 초기화 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "초기화 처리 중 오류")
	})
	@PostMapping("/reset")
	public ResponseEntity<ApiResponse<RankingResetResponse>> resetMonthlyRankings() {

		try {
			String previousMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
			String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

			rankingService.resetMonthlyRankings();

			RankingResetResponse response = RankingResetResponse.builder()
				.success(true)
				.message("월별 랭킹이 성공적으로 초기화되었습니다.")
				.resetMonthYear(previousMonth)
				.newMonthYear(currentMonth)
				.affectedUsers(0) // 실제 영향받은 사용자 수
				.affectedGroups(0) // 실제 영향받은 그룹 수
				.resetAt(LocalDateTime.now())
				.build();

			return ResponseEntity.ok(ApiResponse.success(
				"랭킹 초기화가 완료되었습니다.",
				response
			));

		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(ApiResponse.error(
				"랭킹 초기화 중 오류가 발생했습니다: " + e.getMessage()
			));
		}
	}

	@Operation(
		summary = "사용자 총 점수 조회",
		description = "현재 로그인한 사용자의 총 점수를 조회합니다. " +
			"모든 그룹에서의 활동 점수를 합산한 결과를 제공합니다."
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "총 점수 조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@GetMapping("/me/total-score")
	public ResponseEntity<ApiResponse<Long>> getMyTotalScore(@CurrentUser User currentUser) {

		try {
			long totalScore = rankingService.getTotalScoreByUser(currentUser.getId());

			return ResponseEntity.ok(ApiResponse.success(
				"총 점수 조회가 완료되었습니다.",
				totalScore
			));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(
				"총 점수 조회 중 오류가 발생했습니다: " + e.getMessage()
			));
		}
	}

	@Operation(
		summary = "랭킹 점수 업데이트",
		description = "사용자의 루틴 완료나 인증에 따른 점수를 업데이트합니다. "
	)
	@PostMapping("/update-score")
	public ResponseEntity<ApiResponse<Void>> updateRankingScore(
		@Parameter(description = "사용자 ID", required = true)
		@RequestParam Long userId,
		@Parameter(description = "그룹 ID (개인 점수 업데이트시 null)")
		@RequestParam(required = false) Long groupId,
		@Parameter(description = "추가할 점수", required = true)
		@RequestParam Integer score) {

		try {
			rankingService.updateRankingScore(userId, groupId, score);

			String logMessage = groupId != null ?
				String.format("그룹 점수 업데이트 완료: 사용자 ID = %d, 그룹 ID = %d, 점수 = %d", userId, groupId, score) :
				String.format("개인 점수 업데이트 완료: 사용자 ID = %d, 점수 = %d", userId, score);
			log.info(logMessage);

			return ResponseEntity.ok(ApiResponse.success("점수 업데이트가 완료되었습니다.", null));

		} catch (Exception e) {
			log.error("점수 업데이트 중 오류 발생: 사용자 ID = {}, 그룹 ID = {}, 점수 = {}",
				userId, groupId, score, e);
			return ResponseEntity.badRequest().body(ApiResponse.error(
				"점수 업데이트 중 오류가 발생했습니다: " + e.getMessage()
			));
		}
	}
}