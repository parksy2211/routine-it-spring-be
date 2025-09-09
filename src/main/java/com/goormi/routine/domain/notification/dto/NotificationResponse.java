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
        String senderName = null;
        String groupName = null;

        if (notification.getSender() != null) {
            senderName = notification.getSender().getNickname();
        }

        if (notification.getGroup() != null) {
            groupName = notification.getGroup().getGroupName();
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .notificationType(notification.getNotificationType())
                .senderName(senderName)
                .receiverName(notification.getReceiver().getNickname())
                .groupName(groupName)
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}
