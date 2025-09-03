package com.goormi.routine.domain.notification.entity;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id",nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id",nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false)
    private Boolean isRead;

    private LocalDateTime createdAt;

    public static Notification createNotification(String content, NotificationType type,
                                                  User sender, User receiver, Group group) {
        return Notification.builder()
                .content(content)
                .notificationType(type)
                .sender(sender)
                .receiver(receiver)
                .group(group)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }



}
