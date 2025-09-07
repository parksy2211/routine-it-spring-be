package com.goormi.routine.domain.group.controller;

import com.goormi.routine.domain.group.dto.request.GroupCreateRequest;
import com.goormi.routine.domain.group.dto.request.GroupUpdateRequest;
import com.goormi.routine.domain.group.dto.response.GroupResponse;
import com.goormi.routine.domain.group.entity.GroupType;
import com.goormi.routine.domain.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
@Tag(name = "Group API", description = "그룹 CRUD API")
public class GroupController {

    private final GroupService groupService;

    /**
     * 그룹 생성
     */
    @Operation(summary = "그룹 생성 (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "그룹 생성 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal Long leaderId,
                                                     @Valid @RequestBody GroupCreateRequest request) {
        GroupResponse created = groupService.createGroup(leaderId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 그룹 단일 조회
     */
    @Operation(summary = "그룹 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 조회 성공"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupInfo(@PathVariable Long groupId) {
        GroupResponse response = groupService.getGroupInfo(groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * 그룹 목록 조회 (필터링 가능)
     */
    @Operation(summary = "그룹 리스트 조회", description = "active 그룹들 중 입력값에 따라 필터링하여 보여줌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups(
            @RequestParam(required = false) GroupType groupType,
            @RequestParam(required = false) String category) {

        List<GroupResponse> responses = groupService.getGroupsWithFiltering(groupType, category);
        return ResponseEntity.ok(responses);
    }

    /**
     *  가입된 그룹목록 조회
     */
    @Operation(summary = "가입된 그룹 리스트 조회",
            description = "특정 유저의 가입된 그룹 목록을 조회, 타 사용자의 userId가 입력되지 않으면 본인 목록조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/joined")
    public ResponseEntity<List<GroupResponse>> getJoinedGroups(
            @AuthenticationPrincipal Long myUserId, @RequestParam(required = false) Long userId) {
        Long id = userId != null ? userId : myUserId;
        List<GroupResponse> responses = groupService.getJoinedGroups(id);
        return ResponseEntity.ok(responses);
    }

    /**
     * 리더의 그룹 목록 조회
     */
    @Operation(summary = "리더의 그룹 리스트 조회",
            description = "특정 유저가 리더인 그룹목록을 조회, 타 사용자의 userId가 입력되지 않으면 본인의 목록조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/leader")
    public ResponseEntity<List<GroupResponse>> getGroupsByLeader(@AuthenticationPrincipal Long leaderId,
                                                                 @RequestParam(required = false) Long userId) {
        Long id = userId != null ? userId : leaderId;
        List<GroupResponse> responses = groupService.getGroupsByLeaderId(id);
        return ResponseEntity.ok(responses);
    }

    /**
     * 그룹 정보 수정
     */
    @Operation(summary = "그룹 정보 수정 (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 정보 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없는 사용자"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroupInfo(@AuthenticationPrincipal Long leaderId,
                                                         @PathVariable Long groupId,
                                                         @Valid @RequestBody GroupUpdateRequest request) {
        GroupResponse response = groupService.updateGroupInfo(leaderId, groupId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 그룹 삭제 (비활성화)
     */
    @Operation(summary = "그룹 삭제 (현재 비활성화로 처리)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "프로젝트 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@AuthenticationPrincipal Long leaderId,
                                            @PathVariable Long groupId) {
        groupService.deleteGroup(leaderId, groupId);
        return ResponseEntity.noContent().build();
    }
}
