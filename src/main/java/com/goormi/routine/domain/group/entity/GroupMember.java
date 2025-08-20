package com.goormi.routine.domain.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @Builder
    private GroupMember(Group group, Member member, GroupMemberRole role, GroupMemberStatus status) {
        this.group = group;
        this.member = member;
        this.role = role;
        this.status = status;
    }

    public static GroupMember createGroupMember (Group group, Member member, GroupMemberRole role, GroupMemberStatus status) {
        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .member(member)
                .role(role)
                .status(status)
                .build();

        groupMember.createdAt = LocalDateTime.now();
        groupMember.updatedAt = groupMember.createdAt;

        return groupMember;
    }

    public void changeRole(GroupMemberRole role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(GroupMemberStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }


}
