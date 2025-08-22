package com.goormi.routine.domain.group.controller;

import com.goormi.routine.domain.group.dto.request.GroupJoinRequest;
import com.goormi.routine.domain.group.dto.request.LeaderAnswerRequest;
import com.goormi.routine.domain.group.dto.response.GroupMemberResponse;
import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import com.goormi.routine.domain.group.entity.User;
import com.goormi.routine.domain.group.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group/{groupId}")
@Tag(name = "그룹 멤버 API", description = "특정 그룹에서의 멤버 관리 API")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    /**
     * 그룹에 멤버 가입 요청
     */
    @Operation(summary = "그룹 가입 요청 (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 가입 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
    })
    @PostMapping("/join")
    public ResponseEntity<GroupMemberResponse> joinGroup(/*@AuthenticationPrincipal*/ User user,
                                                                                      @PathVariable Long groupId,
                                                                                      @Valid @RequestBody GroupJoinRequest request) {
        // TODO: 인증 기능 구현 후 @AuthenticationPrincipal 등으로 교체 필요

        GroupMemberResponse response = groupMemberService.addMember(user, groupId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 그룹의 멤버 목록 조회 (필터링 가능)
     */
    @Operation(summary = "그룹 멤버 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 멤버 목록 조회 성공")
    })
    @GetMapping("/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(required = false) GroupMemberRole role,
            @RequestParam(required = false) GroupMemberStatus status) {

        List<GroupMemberResponse> responses;
        if (role != null) {
            responses = groupMemberService.getGroupsByRole(groupId, role);
        } else if (status != null) {
            responses = groupMemberService.getGroupsByStatus(groupId, status);
        } else {
            // 기본적으로는 JOINED 상태의 멤버 목록을 반환
            responses = groupMemberService.getGroupsByStatus(groupId, GroupMemberStatus.JOINED);
        }
        return ResponseEntity.ok(responses);
    }

    /**
     * 그룹 리더가 멤버의 상태를 변경 (신청/가입/차단/떠남)
     */
    @PutMapping("/members/status")
    @Operation(summary = "그룹 멤버 상태 변경 (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 멤버 상태 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한이 없는 사용자")
    })
    public ResponseEntity<GroupMemberResponse> updateMemberStatus(/*@AuthenticationPrincipal*/ User leader,
                                                                               @Valid @RequestBody LeaderAnswerRequest request) {
        // TODO: 임시 User 객체 사용, 인증 기능 구현 후 @AuthenticationPrincipal 등으로 교체 필요
        GroupMemberResponse response = groupMemberService.updateMemberStatus(leader, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 그룹 리더가 멤버의 역할을 변경 (리더 위임 등)
     */
    @PutMapping("/members/role")
    @Operation(summary = "그룹 멤버 역할 변경 (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 멤버 역할 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한이 없는 사용자")
    })
    public ResponseEntity<GroupMemberResponse> updateMemberRole(/*@AuthenticationPrincipal*/ User user,
                                                                             @Valid @RequestBody LeaderAnswerRequest request) {
        // TODO: 임시 User 객체 사용, 인증 기능 구현 후 @AuthenticationPrincipal 등으로 교체 필요
        GroupMemberResponse response = groupMemberService.updateMemberRole(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 그룹 탈퇴 (자발적 탈퇴)
     */
    @Operation(summary = "그룹 탈퇴 (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "그룹 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "그룹이나 유저를 찾을 수 없음")
    })
    @DeleteMapping("/members/me")
    public ResponseEntity<Void> leaveGroup(/*@AuthenticationPrincipal*/ User user,
                                                                        @PathVariable Long groupId) {
        // TODO: 임시 User 객체 사용, 인증 기능 구현 후 @AuthenticationPrincipal 등으로 교체 필요
        groupMemberService.delete(user, groupId);
        return ResponseEntity.noContent().build();
    }
}
