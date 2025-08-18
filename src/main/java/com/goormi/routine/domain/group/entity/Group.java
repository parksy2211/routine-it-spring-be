package com.goormi.routine.domain.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;

    @OneToMany(mappedBy = "groupMembers",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> groupMembers = new ArrayList<>();

    @Column(nullable = false)
    private String groupName;

    @Lob
    private String description;

    @Column(length = 50)
    private String category;

    private String groupImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType groupType;


    private int maxMembers;

    private LocalTime alarmTime;

    @Column(length = 7)
    private String authDays; // ex) 월, 수, 금 -> 0101010 (일 - 토)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isActive;
}
