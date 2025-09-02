package com.goormi.routine.domain.notification.dto;

import com.goormi.routine.domain.notification.entity.Notification;
import com.goormi.routine.domain.notification.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class NotificationResponse {
    private Long id;
    private String content;
    private NotificationType notificationType;

    private String senderName;
    private String receiverName;
    private String groupName;

    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .notificationType(notification.getNotificationType())
                .senderName(notification.getSender().getNickname())
                .receiverName(notification.getReceiver().getNickname())
                .groupName(notification.getGroup().getGroupName())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}
