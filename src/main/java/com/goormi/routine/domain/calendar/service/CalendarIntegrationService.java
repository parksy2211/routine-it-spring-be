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
        
        if (groupMember.getStatus() == GroupMemberStatus.JOINED) {
            // 그룹 가입 시 일정 생성
            handleMemberJoined(groupMember);
        } else {
            // 그룹 탈퇴 시 일정 삭제
            handleMemberLeft(groupMember);
        }
    }

    /**
     * 그룹 정보 변경 시 일정 업데이트
     */
    @EventListener
    @Transactional
    public void handleGroupInfoUpdate(GroupInfoUpdateEvent event) {
        Group group = event.getGroup();
        
        // 그룹의 모든 활성 멤버들의 일정 업데이트
        group.getGroupMembers().stream()
                .filter(member -> member.getStatus() == GroupMemberStatus.JOINED)
                .filter(GroupMember::hasCalendarEvent) // 캘린더 이벤트가 있는 멤버만
                .forEach(member -> {
                    try {
                        Long userId = member.getUser().getId();
                        String eventId = member.getCalendarEventId();
                        
                        if (calendarService.isCalendarConnected(userId)) {
                            calendarService.updateGroupSchedule(userId, group, eventId);
                            log.info("그룹 일정 업데이트 완료: userId={}, groupId={}, eventId={}", 
                                    userId, group.getGroupId(), eventId);
                        }
                    } catch (Exception e) {
                        log.error("그룹 일정 업데이트 실패: groupMemberId={}", member.getMemberId(), e);
                    }
                });
    }

    /**
     * 그룹 삭제 시 모든 관련 일정 삭제
     */
    @EventListener
    @Transactional
    public void handleGroupDeletion(GroupDeletionEvent event) {
        Group group = event.getGroup();
        
        // 그룹의 모든 멤버들의 일정 삭제
        group.getGroupMembers().forEach(member -> 
            handleMemberLeft(member));
    }

    private void handleMemberJoined(GroupMember groupMember) {
        try {
            Long userId = groupMember.getUser().getId();
            Group group = groupMember.getGroup();
            
            if (calendarService.isCalendarConnected(userId)) {
                String eventId = calendarService.createGroupSchedule(userId, group);
                
                // 생성된 eventId를 GroupMember에 저장
                groupMember.updateCalendarEventId(eventId);
                
                log.info("그룹 일정 생성 완료: userId={}, groupId={}, eventId={}", 
                        userId, group.getGroupId(), eventId);
            }
        } catch (Exception e) {
            log.error("그룹 일정 생성 실패: groupMemberId={}", groupMember.getMemberId(), e);
        }
    }

    private void handleMemberLeft(GroupMember groupMember) {
        try {
            Long userId = groupMember.getUser().getId();
            String eventId = groupMember.getCalendarEventId();
            
            // 캘린더 이벤트가 있는 경우에만 삭제
            if (eventId != null && calendarService.isCalendarConnected(userId)) {
                calendarService.deleteGroupSchedule(eventId, userId);
                
                // GroupMember에서 eventId 제거
                groupMember.clearCalendarEventId();
                
                log.info("그룹 일정 삭제 완료: userId={}, eventId={}", userId, eventId);
            }
        } catch (Exception e) {
            log.error("그룹 일정 삭제 실패: groupMemberId={}", groupMember.getMemberId(), e);
        }
    }

    /**
     * 이벤트 클래스들
     */
    public static class GroupMemberStatusChangeEvent {
        private final GroupMember groupMember;

        public GroupMemberStatusChangeEvent(GroupMember groupMember) {
            this.groupMember = groupMember;
        }

        public GroupMember getGroupMember() { return groupMember; }
    }

    public static class GroupInfoUpdateEvent {
        private final Group group;

        public GroupInfoUpdateEvent(Group group) {
            this.group = group;
        }

        public Group getGroup() { return group; }
    }

    public static class GroupDeletionEvent {
        private final Group group;

        public GroupDeletionEvent(Group group) {
            this.group = group;
        }

        public Group getGroup() { return group; }
    }
}
