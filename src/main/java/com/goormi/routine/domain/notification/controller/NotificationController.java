package com.goormi.routine.domain.notification.controller;

import com.goormi.routine.domain.notification.dto.NotificationResponse;
import com.goormi.routine.domain.notification.entity.NotificationType;
import com.goormi.routine.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "notification API", description = "그룹 가입, 그룹멤터 상태 역할 변경 알림 API")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("")
    @Operation(summary = "유저의 알림 전체 조회", description = "인증된 사용자의 전체 알림을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal Long receiverId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByReceiver(receiverId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/Type")
    @Operation(summary = "유저의 알림타입 별 조회", description = "인증된 사용자의 알림을 타입별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(
            @AuthenticationPrincipal Long receiverId,
            @RequestParam NotificationType notificationType) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByNotificationType(receiverId, notificationType);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{notificationId}/read")
    @Operation(summary = "유저의 알림 읽음 표시", description = "인증된 사용자가 알림을 읽었습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<NotificationResponse> readNotification(
            @AuthenticationPrincipal Long userId,
            @PathVariable("notificationId") Long notificationId,
            @RequestParam boolean isRead) {

        notificationService.updateIsRead(notificationId, userId, isRead);
        return ResponseEntity.ok().build();
    }

}
