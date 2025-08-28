package com.goormi.routine.domain.userActivity.controller;

import com.goormi.routine.domain.userActivity.dto.UserActivityRequest;
import com.goormi.routine.domain.userActivity.dto.UserActivityResponse;
import com.goormi.routine.domain.userActivity.service.UserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-activities")
@Tag(name = "UserActivity API", description = "사용자 활동 CRUD API")
public class UserActivityController {

    private final UserActivityService userActivityService;

    @Operation(summary = "특정 날짜의 사용자 활동 목록 조회", description = "인증된 사용자의 특정 날짜 활동 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<List<UserActivityResponse>> getUserActivities(
            @AuthenticationPrincipal Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<UserActivityResponse> activities = userActivityService.getUserActivitiesPerDay(userId, date);
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "새로운 사용자 활동 생성", description = "인증된 사용자의 활동을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "활동 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping
    public ResponseEntity<UserActivityResponse> createActivity(@AuthenticationPrincipal Long userId,
                                                               @RequestBody @Valid UserActivityRequest request) {
        UserActivityResponse response = userActivityService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "사용자 활동 수정", description = "특정 활동의 타입을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
    })
    @PutMapping
    public ResponseEntity<UserActivityResponse> updateActivity(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserActivityRequest request) {
        UserActivityResponse response = userActivityService.updateActivity(userId, request);
        return ResponseEntity.ok(response);
    }
}