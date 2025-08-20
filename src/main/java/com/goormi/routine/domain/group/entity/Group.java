package com.goormi.routine.domain.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @Column(nullable = false)
    private Long leaderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "groupMembers",cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<GroupMember> groupMembers = new ArrayList<>();

    @Column(nullable = false)
    private String groupName;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType groupType;


    private LocalTime alarmTime;

    @Column(length = 7)
    private String authDays; // ex) 월, 수, 금 -> 0101010 (일 - 토)


    @Column(length = 50)
    private String category;

    private String groupImageUrl;

    private int maxMembers;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isActive;


    // 생성자
    @Builder
    private Group(Long leaderId, Member member,String groupName, String description,
                  GroupType groupType, LocalTime alarmTime, String authDays,
                  String category, String groupImageUrl, Integer maxMembers ) {
        //basicInfo
        this.leaderId = leaderId;
        this.member = member;
        this.groupName = groupName;
        this.description = description;
        this.groupType = groupType;
        //timeInfo
        this.alarmTime = alarmTime;
        this.authDays = authDays;
        //optionalInfo
        this.category = category;
        this.groupImageUrl = groupImageUrl;
        this.maxMembers = (maxMembers != null ? maxMembers : 50);
    }

    public static Group createGroup(Long leaderId, String groupName, String description, GroupType groupType) {
        Group group = Group.builder()
                .leaderId(leaderId)
                .groupName(groupName)
                .description(description)
                .groupType(groupType)
                .build();

        // 생성 초기값은 여기서 일괄 세팅
        group.createdAt = LocalDateTime.now();
        group.updatedAt = group.createdAt;
        group.isActive = true;

        return group;
    }

    public void addMember(Member member) {
        GroupMember groupMember = GroupMember.createGroupMember
                (this, member, GroupMemberRole.MEMBER, GroupMemberStatus.PENDING);
        groupMembers.add(groupMember);
    }

    public void addLeader(Member member) {
        GroupMember groupLeader = GroupMember.createGroupMember
                (this,  member, GroupMemberRole.LEADER,  GroupMemberStatus.JOINED);
        groupMembers.add(groupLeader);
    }

    public void updateBasicInfo(String name, String description, GroupType groupType) {
        if (name == null || name.isBlank()) {
            throw new  IllegalArgumentException("name cannot be null or blank");
            // throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.groupName = name;
        this.description = description;
        this.groupType = groupType;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTimeInfo(LocalTime alarmTime, String authDays) {
        this.alarmTime =  alarmTime;
        this.authDays = authDays;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateOptionalInfo(String category, String groupImageUrl, int maxMembers) {
        this.category = category;
        this.groupImageUrl = groupImageUrl;
        this.maxMembers = maxMembers;
        this.updatedAt = LocalDateTime.now();
    }


    public void activate() {
        this.isActive = true;
    }
    public void deactivate() {
        this.isActive = false;
    }


}
