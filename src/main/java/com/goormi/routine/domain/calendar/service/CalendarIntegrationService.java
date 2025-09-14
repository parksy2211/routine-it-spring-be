package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.List;

/**
 * 캘린더와 다른 도메인 간의 연동을 담당하는 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "calendar.integration.enabled", havingValue = "true", matchIfMissing = true)
public class CalendarIntegrationService {

    private final CalendarService calendarService;
    private final CalendarMemberService calendarMemberService;

    @PostConstruct
    public void postConstruct() {
        log.info("CalendarIntegrationService initialized successfully.");
    }

    /**
     * 그룹 멤버 상태 변경 시 캘린더 일정 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupMemberStatusChange(GroupMemberStatusChangeEvent event) {
        log.info("=== 그룹 멤버 상태 변경 이벤트 수신 ===");
        GroupMember groupMember = event.getGroupMember();
        log.info("처리할 그룹 멤버: groupMemberId={}, userId={}, groupId={}, status={}", 
                groupMember.getMemberId(),
                groupMember.getUser().getId(), 
                groupMember.getGroup().getGroupId(), 
                groupMember.getStatus());
        
        if (groupMember.getStatus() == GroupMemberStatus.JOINED) {
            // 그룹 가입 시 일정 생성 - 별도 서비스로 위임
            log.info("그룹 가입 처리 시작: userId={}, groupId={}", 
                    groupMember.getUser().getId(), groupMember.getGroup().getGroupId());
            calendarMemberService.handleMemberJoined(groupMember);
        } else {
            // 그룹 탈퇴 시 일정 삭제 - 별도 서비스로 위임
            log.info("그룹 탈퇴 처리 시작: userId={}, groupId={}, status={}", 
                    groupMember.getUser().getId(), groupMember.getGroup().getGroupId(), groupMember.getStatus());
            calendarMemberService.handleMemberLeft(groupMember);
        }
        
        log.info("그룹 멤버 상태 변경 이벤트 처리 완료 ");
        log.info("그룹멤버데이터: groupMemberID={}, groupId={}",
                groupMember.getMemberId(), groupMember.getGroup().getGroupId());
    }

    /**
     * 그룹 정보 변경 시 일정 업데이트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupInfoUpdate(GroupInfoUpdateEvent event) {
        Group group = event.getGroup();
        log.info("그룹 정보 변경 이벤트 수신: groupId={}, groupName={}", 
                group.getGroupId(), group.getGroupName());
        
        // 디버깅: 전체 멤버 수 확인
        List<GroupMember> allMembers = group.getGroupMembers();
        log.info("그룹 전체 멤버 수: {}", allMembers.size());
        
        // 활성 멤버 필터링
        List<GroupMember> joinedMembers = allMembers.stream()
                .filter(member -> member.getStatus() == GroupMemberStatus.JOINED)
                .toList();
        log.info("활성 멤버 수 (JOINED): {}", joinedMembers.size());
        
        // 캘린더 이벤트가 있는 멤버 필터링
        List<GroupMember> membersWithCalendarEvent = joinedMembers.stream()
                .filter(GroupMember::hasCalendarEvent)
                .toList();
        log.info("캘린더 이벤트가 있는 멤버 수: {}", membersWithCalendarEvent.size());
        
        // 디버깅: 캘린더 이벤트가 없는 멤버들 확인
        joinedMembers.forEach(member -> {
            String eventId = member.getCalendarEventId();
            log.info("멤버 상세: userId={}, calendarEventId={}, hasCalendarEvent={}", 
                    member.getUser().getId(), 
                    eventId != null ? eventId : "null", 
                    member.hasCalendarEvent());
        });
        
        // 그룹의 모든 활성 멤버들의 일정 업데이트
        membersWithCalendarEvent.forEach(member -> {
            try {
                Long userId = member.getUser().getId();
                String eventId = member.getCalendarEventId();
                
                log.info("캘린더 일정 업데이트 시도: userId={}, eventId={}", userId, eventId);
                
                if (calendarService.isCalendarConnected(userId)) {
                    calendarService.updateGroupSchedule(userId, group, eventId);
                    log.info("그룹 일정 업데이트 완료: userId={}, groupId={}, eventId={}", 
                            userId, group.getGroupId(), eventId);
                } else {
                    log.warn("캘린더 연동 안됨 - 일정 업데이트 스킵: userId={}", userId);
                }
            } catch (Exception e) {
                log.error("그룹 일정 업데이트 실패: groupMemberId={}, userId={}", 
                        member.getMemberId(), member.getUser().getId(), e);
            }
        });
        
        log.info("그룹 정보 변경 이벤트 처리 완료: 총 {}명의 멤버 처리", membersWithCalendarEvent.size());
    }

    /**
     * 그룹 삭제 시 모든 관련 일정 삭제
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupDeletion(GroupDeletionEvent event) {
        Group group = event.getGroup();
        log.info("그룹 삭제 이벤트 수신: groupId={}, groupName={}", 
                group.getGroupId(), group.getGroupName());
        
        // 디버깅: 전체 멤버 수 확인
        List<GroupMember> allMembers = group.getGroupMembers();
        log.info("그룹 전체 멤버 수: {}", allMembers.size());
        
        // 활성 멤버 필터링
        List<GroupMember> joinedMembers = allMembers.stream()
                .filter(member -> member.getStatus() == GroupMemberStatus.JOINED)
                .toList();
        log.info("활성 멤버 수 (JOINED): {}", joinedMembers.size());
        
        // 캘린더 이벤트가 있는 멤버 필터링
        List<GroupMember> membersWithCalendarEvent = joinedMembers.stream()
                .filter(GroupMember::hasCalendarEvent)
                .toList();
        log.info("캘린더 이벤트가 있는 멤버 수: {}", membersWithCalendarEvent.size());
        
        // 디버깅: 캘린더 이벤트가 없는 멤버들 확인
        joinedMembers.forEach(member -> {
            String eventId = member.getCalendarEventId();
            log.info("멤버 상세: userId={}, calendarEventId={}, hasCalendarEvent={}", 
                    member.getUser().getId(), 
                    eventId != null ? eventId : "null", 
                    member.hasCalendarEvent());
        });
        
        // 그룹의 모든 멤버들의 일정 삭제
        membersWithCalendarEvent.forEach(member -> {
            try {
                Long userId = member.getUser().getId();
                String eventId = member.getCalendarEventId();
                
                log.info("캘린더 일정 삭제 시도: userId={}, eventId={}", userId, eventId);
                
                calendarMemberService.handleMemberLeft(member);
                log.info("그룹 삭제로 인한 일정 삭제 완료: userId={}, groupId={}", 
                        member.getUser().getId(), group.getGroupId());
            } catch (Exception e) {
                log.error("그룹 삭제 시 일정 삭제 실패: userId={}, groupId={}", 
                        member.getUser().getId(), group.getGroupId(), e);
            }
        });
        
        log.info("그룹 삭제 이벤트 처리 완료: 총 {}명의 멤버 처리", membersWithCalendarEvent.size());
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
