package com.goormi.routine.personal_routines.controller;

import com.goormi.routine.personal_routines.dto.PersonalRoutineRequest;
import com.goormi.routine.personal_routines.dto.PersonalRoutineResponse;
import com.goormi.routine.personal_routines.dto.PersonalRoutineUpdateRequest;
import com.goormi.routine.personal_routines.service.PersonalRoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-routines")
@Tag(name = "PersonalRoutine API", description = "개인 루틴 관련 API")
public class PersonalRoutineController {

    private final PersonalRoutineService service;

    @Operation(summary = "개인 루틴 생성", description = "새로운 개인 루틴을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "루틴 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<PersonalRoutineResponse> create(
            @Valid @RequestBody PersonalRoutineRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(summary = "사용자별 루틴 조회", description = "특정 사용자의 모든 개인 루틴 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PersonalRoutineResponse>> listByUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Integer userId) {
        return ResponseEntity.ok(service.listByUser(userId));
    }

    @Operation(summary = "루틴 단건 조회", description = "루틴 ID로 개인 루틴을 조회합니다.")
    @GetMapping("/{routineId}")
    public ResponseEntity<PersonalRoutineResponse> get(
            @Parameter(description = "루틴 ID", required = true) @PathVariable Integer routineId) {
        return ResponseEntity.ok(service.get(routineId));
    }

    @Operation(summary = "루틴 수정", description = "기존 개인 루틴의 정보를 수정합니다.")
    @PatchMapping("/{routineId}")
    public ResponseEntity<PersonalRoutineResponse> update(
            @Parameter(description = "루틴 ID", required = true) @PathVariable Integer routineId,
            @Valid @RequestBody PersonalRoutineUpdateRequest req) {
        return ResponseEntity.ok(service.update(routineId, req));
    }

    @Operation(summary = "루틴 삭제", description = "개인 루틴을 소프트 삭제합니다.")
    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "루틴 ID", required = true) @PathVariable Integer routineId) {
        service.softDelete(routineId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알림 on/off 토글", description = "특정 루틴의 알림 여부를 토글합니다.")
    @PostMapping("/{routineId}/toggle-alarm")
    public ResponseEntity<PersonalRoutineResponse> toggleAlarm(
            @Parameter(description = "루틴 ID", required = true) @PathVariable Integer routineId) {
        return ResponseEntity.ok(service.toggleAlarm(routineId));
    }

    @Operation(summary = "공개 여부 on/off 토글", description = "특정 루틴의 공개 여부를 토글합니다.")
    @PostMapping("/{routineId}/toggle-public")
    public ResponseEntity<PersonalRoutineResponse> togglePublic(
            @Parameter(description = "루틴 ID", required = true) @PathVariable Integer routineId) {
        return ResponseEntity.ok(service.togglePublic(routineId));
    }
}
