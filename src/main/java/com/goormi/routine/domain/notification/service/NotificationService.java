package com.goormi.routine.domain.notification.service;

import com.goormi.routine.domain.notification.dto.NotificationResponse;
import com.goormi.routine.domain.notification.entity.NotificationType;

import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(NotificationType notificationType,
                                            Long senderId, Long receiverId, Long groupId);

    List<NotificationResponse> getNotificationsByReceiver(Long receiverId);

    List<NotificationResponse> getNotificationsByNotificationType(Long receiverId, NotificationType notificationType);

    void updateIsRead(Long notificationId, Long receiverId, Boolean isRead);
}
