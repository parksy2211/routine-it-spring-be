package com.goormi.routine.personal_routines.controller;

import com.goormi.routine.personal_routines.dto.PersonalRoutineRequest;
import com.goormi.routine.personal_routines.dto.PersonalRoutineResponse;
import com.goormi.routine.personal_routines.dto.PersonalRoutineUpdateRequest;
import com.goormi.routine.personal_routines.service.PersonalRoutineService;
import com.goormi.routine.personal_routines.service.RoutineExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-routines")
@Tag(name = "Personal Routine API", description = "개인 루틴 관리 API")
public class PersonalRoutineController {

    private final PersonalRoutineService service;
    private final RoutineExecutionService executionService;

    @Operation(summary = "개인 루틴 생성", description = "새로운 개인 루틴을 등록합니다.")
    @PostMapping
    public ResponseEntity<PersonalRoutineResponse> create(@Valid @RequestBody PersonalRoutineRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(summary = "사용자별 루틴 목록 조회", description = "특정 사용자의 개인 루틴 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PersonalRoutineResponse>> listByUser(
            @Parameter(description = "사용자 ID") @PathVariable Integer userId) {
        return ResponseEntity.ok(service.listByUser(userId));
    }

    @Operation(summary = "개인 루틴 상세 조회", description = "루틴 ID로 개인 루틴 상세 정보를 조회합니다.")
    @GetMapping("/{routineId}")
    public ResponseEntity<PersonalRoutineResponse> get(
            @Parameter(description = "루틴 ID") @PathVariable Integer routineId) {
        return ResponseEntity.ok(service.get(routineId));
    }

    @Operation(summary = "개인 루틴 수정", description = "루틴 ID로 개인 루틴 정보를 수정합니다.")
    @PatchMapping("/{routineId}")
    public ResponseEntity<PersonalRoutineResponse> update(
            @Parameter(description = "루틴 ID") @PathVariable Integer routineId,
            @Valid @RequestBody PersonalRoutineUpdateRequest req) {
        return ResponseEntity.ok(service.update(routineId, req));
    }

    @Operation(summary = "개인 루틴 삭제", description = "개인 루틴을 소프트 삭제합니다.")
    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "루틴 ID") @PathVariable Integer routineId) {
        service.softDelete(routineId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알람 토글", description = "루틴 알람 설정을 켜거나 끕니다.")
    @PostMapping("/{routineId}/toggle-alarm")
    public ResponseEntity<PersonalRoutineResponse> toggleAlarm(
            @Parameter(description = "루틴 ID") @PathVariable Integer routineId) {
        return ResponseEntity.ok(service.toggleAlarm(routineId));
    }

    @Operation(summary = "공개 여부 토글", description = "루틴 공개 여부를 변경합니다.")
    @PostMapping("/{routineId}/toggle-public")
    public ResponseEntity<PersonalRoutineResponse> togglePublic(
            @Parameter(description = "루틴 ID") @PathVariable Integer routineId) {
        return ResponseEntity.ok(service.togglePublic(routineId));
    }

    @Operation(summary = "루틴 완료 처리", description = "루틴을 완료 처리하고 자동 출석을 트리거합니다.")
    @PostMapping("/{routineId}/done")
    public ResponseEntity<Void> done(
            @Parameter(description = "루틴 ID") @PathVariable Integer routineId,
            @Parameter(description = "사용자 ID") @RequestParam Integer userId,
            @Parameter(description = "완료 날짜 (YYYY-MM-DD)") @RequestParam(required = false) String date) {
        executionService.markDone(
                userId,
                routineId,
                (date != null ? LocalDate.parse(date) : null)
        );
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "루틴 완료 취소", description = "루틴 완료를 취소합니다. (기본 정책: 출석은 유지)")
    @DeleteMapping("/{routineId}/done")
    public ResponseEntity<Void> undo(
            @Parameter(description = "루틴 ID") @PathVariable Integer routineId,
            @Parameter(description = "사용자 ID") @RequestParam Integer userId,
            @Parameter(description = "완료 날짜 (YYYY-MM-DD)") @RequestParam String date) {
        executionService.undo(userId, routineId, LocalDate.parse(date));
        return ResponseEntity.noContent().build();
    }
}
