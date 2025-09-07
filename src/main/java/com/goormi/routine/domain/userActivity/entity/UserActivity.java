package com.goormi.routine.domain.userActivity.entity;

import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.personal_routines.domain.PersonalRoutine;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserActivity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private LocalDate activityDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private PersonalRoutine  personalRoutine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private GroupMember groupMember;

    private String imageUrl;
    private boolean isPublic;

    public static UserActivity createActivity (User user,PersonalRoutine personalRoutine) {
        return UserActivity.builder()
                .user(user)
                .personalRoutine(personalRoutine)
                .activityType(ActivityType.PERSONAL_ROUTINE_COMPLETE)
                .activityDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .isPublic(false)
                .build();
    }
    public static UserActivity createActivity (User user, GroupMember groupMember, String imageUrl) {
        return UserActivity.builder()
                .user(user)
                .groupMember(groupMember)
                .imageUrl(imageUrl)
                .activityType(ActivityType.GROUP_AUTH_COMPLETE)
                .activityDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .isPublic(false)
                .build();
    }

    public void updateActivity(ActivityType activityType, boolean isPublic) {
        this.activityType = activityType;
        this.isPublic = isPublic;

        if (this.activityDate == null) {
            this.activityDate = LocalDate.now();
        }

        if (activityType == null) {
            this.activityDate = null;
        }
        this.updatedAt = LocalDateTime.now();
    }





}
