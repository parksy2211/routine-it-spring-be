package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 캘린더와 다른 도메인 간의 연동을 담당하는 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarIntegrationService {

    private final CalendarService calendarService;

    /**
     * 그룹 멤버 상태 변경 시 캘린더 일정 처리
     */
    @EventListener
    @Transactional
    public void handleGroupMemberStatusChange(GroupMemberStatusChangeEvent event) {
        GroupMember groupMember = event.getGroupMember();
        String accessToken = event.getAccessToken();
        
        if (groupMember.getStatus() == GroupMemberStatus.JOINED) {
            // 그룹 가입 시 일정 생성
            handleMemberJoined(groupMember, accessToken);
        } else {
            // 그룹 탈퇴 시 일정 삭제
            handleMemberLeft(groupMember, accessToken);
        }
    }

    /**
     * 그룹 정보 변경 시 일정 업데이트
     */
    @EventListener
    @Transactional
    public void handleGroupInfoUpdate(GroupInfoUpdateEvent event) {
        Group group = event.getGroup();
        String accessToken = event.getAccessToken();
        
        // 그룹의 모든 활성 멤버들의 일정 업데이트
        group.getGroupMembers().stream()
                .filter(member -> member.getStatus() == GroupMemberStatus.JOINED)
                .forEach(member -> updateMemberSchedule(member, group, accessToken));
    }

    /**
     * 그룹 삭제 시 모든 관련 일정 삭제
     */
    @EventListener
    @Transactional
    public void handleGroupDeletion(GroupDeletionEvent event) {
        Group group = event.getGroup();
        String accessToken = event.getAccessToken();
        
        // 그룹의 모든 멤버들의 일정 삭제
        group.getGroupMembers().forEach(member -> 
            handleMemberLeft(member, accessToken));
    }

    private void handleMemberJoined(GroupMember groupMember, String accessToken) {
        try {
            Long userId = groupMember.getUser().getId();
            Group group = groupMember.getGroup();
            
            if (calendarService.isCalendarConnected(userId)) {
                String eventId = calendarService.createGroupSchedule(userId, group);
                log.info("그룹 일정 생성 완료: userId={}, groupId={}, eventId={}", 
                        userId, group.getGroupId(), eventId);
            }
        } catch (Exception e) {
            log.error("그룹 일정 생성 실패: groupMemberId={}", groupMember.getMemberId(), e);
        }
    }

    private void handleMemberLeft(GroupMember groupMember, String accessToken) {
        try {
            // 실제 구현에서는 GroupMember에 eventId를 저장하거나 
            // 별도 테이블로 관리해야 함
            // 여기서는 예시로 처리
            log.info("그룹 일정 삭제 처리: groupMemberId={}", groupMember.getMemberId());
        } catch (Exception e) {
            log.error("그룹 일정 삭제 실패: groupMemberId={}", groupMember.getMemberId(), e);
        }
    }

    private void updateMemberSchedule(GroupMember groupMember, Group group, String accessToken) {
        try {
            Long userId = groupMember.getUser().getId();
            // 실제 구현에서는 eventId를 가져와서 업데이트
            log.info("그룹 일정 업데이트 처리: userId={}, groupId={}", 
                    userId, group.getGroupId());
        } catch (Exception e) {
            log.error("그룹 일정 업데이트 실패: groupMemberId={}", groupMember.getMemberId(), e);
        }
    }

    /**
     * 이벤트 클래스들
     */
    public static class GroupMemberStatusChangeEvent {
        private final GroupMember groupMember;
        private final String accessToken;

        public GroupMemberStatusChangeEvent(GroupMember groupMember, String accessToken) {
            this.groupMember = groupMember;
            this.accessToken = accessToken;
        }

        public GroupMember getGroupMember() { return groupMember; }
        public String getAccessToken() { return accessToken; }
    }

    public static class GroupInfoUpdateEvent {
        private final Group group;
        private final String accessToken;

        public GroupInfoUpdateEvent(Group group, String accessToken) {
            this.group = group;
            this.accessToken = accessToken;
        }

        public Group getGroup() { return group; }
        public String getAccessToken() { return accessToken; }
    }

    public static class GroupDeletionEvent {
        private final Group group;
        private final String accessToken;

        public GroupDeletionEvent(Group group, String accessToken) {
            this.group = group;
            this.accessToken = accessToken;
        }

        public Group getGroup() { return group; }
        public String getAccessToken() { return accessToken; }
    }
}
