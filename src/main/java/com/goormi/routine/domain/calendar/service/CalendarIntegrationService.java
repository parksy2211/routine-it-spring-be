package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import com.goormi.routine.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import jakarta.annotation.PostConstruct;

/**
 * 캘린더와 다른 도메인 간의 연동을 담당하는 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "calendar.integration.enabled", havingValue = "true", matchIfMissing = true)
public class CalendarIntegrationService {

    private final CalendarService calendarService;
    private final KakaoTokenService kakaoTokenService;

    @PostConstruct
    public void postConstruct() {
        log.info("CalendarIntegrationService initialized successfully.");
    }

    /**
     * 그룹 멤버 상태 변경 시 캘린더 일정 처리
     */
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupMemberStatusChange(GroupMemberStatusChangeEvent event) {
        log.info("=== 그룹 멤버 상태 변경 이벤트 처리 시작 ===");
        GroupMember groupMember = event.getGroupMember();
        log.debug("처리할 그룹 멤버: userId={}, groupId={}, status={}", 
                groupMember.getUser().getId(), 
                groupMember.getGroup().getGroupId(), 
                groupMember.getStatus());
        
        if (groupMember.getStatus() == GroupMemberStatus.JOINED) {
            // 그룹 가입 시 일정 생성
            log.debug("그룹 가입 처리 시작");
            handleMemberJoined(groupMember);
        } else {
            // 그룹 탈퇴 시 일정 삭제
            log.debug("그룹 탈퇴 처리 시작");
            handleMemberLeft(groupMember);
        }
        
        log.info("그룹 멤버 상태 변경 이벤트 처리 완료");
    }

    /**
     * 그룹 정보 변경 시 일정 업데이트
     */
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupDeletion(GroupDeletionEvent event) {
        Group group = event.getGroup();
        
        // 그룹의 모든 멤버들의 일정 삭제
        group.getGroupMembers().forEach(member -> 
            handleMemberLeft(member));
    }

    private void handleMemberJoined(GroupMember groupMember) {
        log.debug("handleMemberJoined 시작: groupMemberId={}", groupMember.getMemberId());
        try {
            Long userId = groupMember.getUser().getId();
            Group group = groupMember.getGroup();
            log.debug("사용자 ID: {}, 그룹 ID: {}", userId, group.getGroupId());
            
            // 1. 카카오 OAuth 토큰이 있는지 확인
            User user = groupMember.getUser();
            log.debug("카카오 리프레시 토큰 존재 여부: {}", user.getKakaoRefreshToken() != null);
            if (user.getKakaoRefreshToken() == null) {
                log.info("카카오 토큰이 없어 캘린더 연동을 건너뜁니다: userId={}", userId);
                return;
            }
            
            // 2. 서브 캘린더가 없다면 생성 (처음 그룹 가입 시)
            log.debug("캘린더 연동 상태 확인 중: userId={}", userId);
            boolean isConnected = calendarService.isCalendarConnected(userId);
            log.debug("캘린더 연동 상태: {}", isConnected);
            if (!isConnected) {
                log.debug("서브 캘린더 생성 필요: userId={}", userId);
                String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
                calendarService.createUserCalendar(userId, accessToken);
                log.info("첫 그룹 가입으로 서브 캘린더 생성: userId={}", userId);
            } else {
                log.info("이미 캘린더가 연동되어 있어 서브 캘린더 생성을 건너뜁니다: userId={}", userId);
            }
            
            // 3. 그룹 일정 생성
            log.debug("그룹 일정 생성 시작: userId={}, groupId={}", userId, group.getGroupId());
            String eventId = calendarService.createGroupSchedule(userId, group);
            log.debug("그룹 일정 생성 결과 eventId: {}", eventId);
            
            // 4. 생성된 eventId를 GroupMember에 저장
            groupMember.updateCalendarEventId(eventId);
            
            log.info("그룹 일정 생성 완료: userId={}, groupId={}, eventId={}", 
                    userId, group.getGroupId(), eventId);
        } catch (Exception e) {
            log.error("그룹 일정 생성 실패: groupMemberId={}, 에러: {}", groupMember.getMemberId(), e.getMessage(), e);
        }
        log.debug("handleMemberJoined 종료: groupMemberId={}", groupMember.getMemberId());
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
