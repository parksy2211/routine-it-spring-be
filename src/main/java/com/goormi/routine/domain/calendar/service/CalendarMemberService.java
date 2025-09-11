package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

/**
 * 캘린더와 그룹 멤버 간의 연동을 처리하는 트랜잭션 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarMemberService {

    private final CalendarService calendarService;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 그룹 멤버 가입 시 캘린더 일정 생성 (트랜잭션 적용)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberJoined(GroupMember groupMember) {
        log.debug("=== CalendarMemberService.handleMemberJoined 시작 ===");
        log.debug("@Transactional 적용된 메서드 실행: groupMemberId={}", groupMember.getMemberId());
        
        try {
            Long userId = groupMember.getUser().getId();
            Group group = groupMember.getGroup();
            log.debug("사용자 ID: {}, 그룹 ID: {}", userId, group.getGroupId());
            
            // 캘린더 연동 여부 확인
            boolean isConnected = calendarService.isCalendarConnected(userId);
            log.debug("캘린더 연동 상태 확인 결과: userId={}, isConnected={}", userId, isConnected);
            
            if (!isConnected) {
                log.info("캘린더가 연동되지 않은 사용자입니다. 캘린더 연동 후 그룹 일정을 생성해주세요: userId={}", userId);
                return;
            }
            
            // 그룹 일정 생성
            log.debug("그룹 일정 생성 시작: userId={}, groupId={}", userId, group.getGroupId());
            String eventId = calendarService.createGroupSchedule(userId, group);
            
            // eventId 값 유효성 검증 및 상세 로그
            log.info("=== eventId 유효성 검증 ===");
            log.info("createGroupSchedule 반환값: eventId=[{}]", eventId);
            log.info("eventId null 체크: {}", eventId == null ? "NULL" : "NOT NULL");
            log.info("eventId 길이: {}", eventId != null ? eventId.length() : "N/A");
            log.info("eventId 공백 체크: {}", eventId != null && eventId.trim().isEmpty() ? "BLANK" : "NOT BLANK");
            
            if (eventId == null || eventId.trim().isEmpty()) {
                log.error("캘린더 일정 생성 실패: eventId가 null 또는 공백입니다");
                throw new IllegalStateException("캘린더 일정 생성 실패: eventId가 유효하지 않습니다");
            }

            // GroupMember ID 확인 및 처리
            log.info("=== GroupMember 상태 확인 ===");
            log.info("전달받은 GroupMember ID: {}", groupMember.getMemberId());
            log.info("전달받은 GroupMember가 null인지: {}", groupMember == null);
            
            GroupMember managedGroupMember;
            
            // 전달받은 GroupMember의 ID가 null인 경우 (아직 영속화되지 않은 경우)
            if (groupMember.getMemberId() == null) {
                log.info("GroupMember ID가 null입니다. 전달받은 객체를 직접 사용합니다.");
                managedGroupMember = groupMember;
            } else {
                // ID가 있는 경우 데이터베이스에서 조회 시도
                log.info("GroupMember ID가 존재합니다. 데이터베이스에서 조회를 시도합니다.");
                managedGroupMember = groupMemberRepository.findById(groupMember.getMemberId())
                        .orElseGet(() -> {
                            log.warn("데이터베이스에서 GroupMember를 찾을 수 없습니다. 전달받은 객체를 사용합니다.");
                            return groupMember;
                        });
            }
            
            log.info("=== GroupMember 업데이트 전 상태 ===");
            log.info("사용할 GroupMember ID: {}", managedGroupMember.getMemberId());
            log.info("업데이트 전 calendarEventId: [{}]", managedGroupMember.getCalendarEventId());

            // 생성된 eventId를 GroupMember에 저장
            managedGroupMember.updateCalendarEventId(eventId);
            
            log.info("=== GroupMember 업데이트 후 상태 ===");
            log.info("업데이트 후 calendarEventId: [{}]", managedGroupMember.getCalendarEventId());
            log.info("hasCalendarEvent(): {}", managedGroupMember.hasCalendarEvent());

            // 데이터베이스에 명시적으로 저장
            log.info("=== 트랜잭션 상태 확인 ===");
            log.info("현재 트랜잭션 활성화 여부: {}", org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
            log.info("현재 트랜잭션 이름: {}", org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
            
            GroupMember savedGroupMember;
            try {
                savedGroupMember = groupMemberRepository.save(managedGroupMember);
                log.info("GroupMember 저장 성공: ID={}", savedGroupMember.getMemberId());
            } catch (Exception e) {
                log.error("GroupMember 저장 실패: {}", e.getMessage());
                // 저장에 실패하더라도 eventId는 메모리 상에서 설정되었으므로 계속 진행
                savedGroupMember = managedGroupMember;
            }
            
            log.info("=== 저장 후 검증 ===");
            log.debug("GroupMember calendarEventId 저장 완료: groupMemberId={}, eventId={}", 
                    savedGroupMember.getMemberId(), savedGroupMember.getCalendarEventId());
            log.info("저장된 GroupMember의 eventId: [{}]", savedGroupMember.getCalendarEventId());
            log.info("저장된 GroupMember의 hasCalendarEvent(): {}", savedGroupMember.hasCalendarEvent());
            
            // flush를 통한 즉시 DB 반영 확인 (트랜잭션 내에서만 수행)
            if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
                try {
                    groupMemberRepository.flush();
                    log.info("Repository flush 완료 - DB 즉시 반영");
                } catch (Exception e) {
                    log.warn("Repository flush 실패: {}", e.getMessage());
                }
                
                // DB에서 다시 조회하여 실제 저장 확인 (ID가 있는 경우에만)
                if (savedGroupMember.getMemberId() != null) {
                    try {
                        GroupMember verificationMember = groupMemberRepository.findById(savedGroupMember.getMemberId())
                                .orElse(null);
                        if (verificationMember != null) {
                            log.info("=== DB 재조회 검증 ===");
                            log.info("DB에서 재조회한 eventId: [{}]", verificationMember.getCalendarEventId());
                            log.info("DB 재조회 hasCalendarEvent(): {}", verificationMember.hasCalendarEvent());
                        } else {
                            log.warn("DB 재조회 결과: GroupMember를 찾을 수 없습니다");
                        }
                    } catch (Exception e) {
                        log.warn("DB 재조회 실패: {}", e.getMessage());
                    }
                } else {
                    log.info("GroupMember ID가 null이므로 DB 재조회를 건너뜁니다");
                }
            } else {
                log.warn("트랜잭션이 비활성화 상태입니다. flush 및 재조회를 건너뜁니다.");
            }
            
            log.info("그룹 일정 생성 완료: userId={}, groupId={}, eventId={}", 
                    userId, group.getGroupId(), eventId);
                    
        } catch (Exception e) {
            log.error("그룹 일정 생성 실패: groupMemberId={}, 에러: {}", groupMember.getMemberId(), e.getMessage(), e);
            throw e; // @Transactional 롤백을 위해 예외 재발생
        }
        
        log.debug("=== CalendarMemberService.handleMemberJoined 종료 ===");
    }

    /**
     * 그룹 멤버 탈퇴 시 캘린더 일정 삭제 (트랜잭션 적용)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberLeft(GroupMember groupMember) {
        log.debug("=== CalendarMemberService.handleMemberLeft 시작 ===");
        log.debug("@Transactional 적용된 메서드 실행: groupMemberId={}", groupMember.getMemberId());
        
        try {
            Long userId = groupMember.getUser().getId();
            String eventId = groupMember.getCalendarEventId();
            
            log.info("그룹 멤버 탈퇴 처리 시작: userId={}, eventId={}", userId, eventId);
            
            // 캘린더 이벤트가 있는 경우에만 삭제
            if (eventId != null && calendarService.isCalendarConnected(userId)) {
                calendarService.deleteGroupSchedule(eventId, userId);
                
                // GroupMember 처리 (안전하게)
                GroupMember managedGroupMember;
                if (groupMember.getMemberId() == null) {
                    log.info("GroupMember ID가 null입니다. 전달받은 객체를 직접 사용합니다.");
                    managedGroupMember = groupMember;
                } else {
                    managedGroupMember = groupMemberRepository.findById(groupMember.getMemberId())
                            .orElseGet(() -> {
                                log.warn("데이터베이스에서 GroupMember를 찾을 수 없습니다. 전달받은 객체를 사용합니다.");
                                return groupMember;
                            });
                }
                
                log.info("=== GroupMember 제거 전 상태 ===");
                log.info("제거 전 calendarEventId: [{}]", managedGroupMember.getCalendarEventId());
                
                // GroupMember에서 eventId 제거
                managedGroupMember.clearCalendarEventId();
                
                log.info("=== GroupMember 제거 후 상태 ===");
                log.info("제거 후 calendarEventId: [{}]", managedGroupMember.getCalendarEventId());
                
                // 데이터베이스에 명시적으로 저장
                log.info("=== 트랜잭션 상태 확인 ===");
                log.info("현재 트랜잭션 활성화 여부: {}", org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
                
                GroupMember savedGroupMember;
                try {
                    savedGroupMember = groupMemberRepository.save(managedGroupMember);
                    log.info("GroupMember 저장 성공: ID={}", savedGroupMember.getMemberId());
                } catch (Exception e) {
                    log.error("GroupMember 저장 실패: {}", e.getMessage());
                    savedGroupMember = managedGroupMember;
                }
                
                // flush를 통한 즉시 DB 반영 확인 (트랜잭션 내에서만 수행)
                if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
                    try {
                        groupMemberRepository.flush();
                        log.info("Repository flush 완료 - DB 즉시 반영");
                    } catch (Exception e) {
                        log.warn("Repository flush 실패: {}", e.getMessage());
                    }
                } else {
                    log.warn("트랜잭션이 비활성화 상태입니다. flush를 건너뜁니다.");
                }
                
                log.debug("GroupMember calendarEventId 제거 완료: groupMemberId={}, eventId={}", 
                        savedGroupMember.getMemberId(), savedGroupMember.getCalendarEventId());
                
                log.info("그룹 일정 삭제 완료: userId={}, eventId={}", userId, eventId);
            } else {
                log.debug("삭제할 캘린더 이벤트가 없거나 캘린더 연동이 안됨: userId={}, eventId={}", userId, eventId);
            }
            
        } catch (Exception e) {
            log.error("그룹 멤버 탈퇴 시 캘린더 일정 삭제 실패: userId={}", 
                    groupMember.getUser().getId(), e);
            throw e; // @Transactional 롤백을 위해 예외 재발생
        }
        
        log.debug("=== CalendarMemberService.handleMemberLeft 종료 ===");
    }
}
