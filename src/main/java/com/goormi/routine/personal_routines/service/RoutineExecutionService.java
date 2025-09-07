package com.goormi.routine.personal_routines.service;

import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import com.goormi.routine.domain.userActivity.entity.ActivityType;
import com.goormi.routine.domain.userActivity.entity.UserActivity;
import com.goormi.routine.domain.userActivity.repository.UserActivityRepository;
import com.goormi.routine.personal_routines.domain.PersonalRoutine;
import com.goormi.routine.personal_routines.domain.RoutineExecution;
import com.goormi.routine.personal_routines.event.RoutineCompletedEvent;
import com.goormi.routine.personal_routines.repository.PersonalRoutineRepository;
import com.goormi.routine.personal_routines.repository.RoutineExecutionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
public class RoutineExecutionService {

    private final RoutineExecutionRepository execRepo;
    private final PersonalRoutineRepository routineRepo;
    private final ApplicationEventPublisher events;

    // >>> 추가 의존성
    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 완료 처리(멱등): 같은 날 같은 루틴은 1회만 기록 */
    @Transactional
    public void markDone(Integer userId, Integer routineId, LocalDate dateOpt) {
        LocalDate day = (dateOpt != null) ? dateOpt : LocalDate.now(KST);

        PersonalRoutine r = routineRepo.findByRoutineIdAndIsDeletedFalse(routineId)
                .orElseThrow(() -> new EntityNotFoundException("루틴을 찾을 수 없습니다."));
        if (!r.getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 루틴의 소유자가 아닙니다.");
        }

        // 실행 로그 멱등 생성
        execRepo.findByUserIdAndRoutineIdAndExecDate(userId, routineId, day)
                .orElseGet(() -> execRepo.save(
                        RoutineExecution.builder()
                                .userId(userId)
                                .routineId(routineId)
                                .execDate(day)
                                .executedAt(LocalDateTime.now(KST))
                                .build()
                ));

        // >>> 활동 레코드 멱등 생성 (카운트 ↑)
        User user = userRepository.findById(r.getUserId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        boolean exists = userActivityRepository
                .findByUserIdAndPersonalRoutine_RoutineIdAndActivityTypeAndActivityDate(
                        user.getId(), routineId, ActivityType.PERSONAL_ROUTINE_COMPLETE, day)
                .isPresent();

        if (!exists) {
            UserActivity ua = UserActivity.builder()
                    .user(user)
                    .personalRoutine(r)
                    .activityType(ActivityType.PERSONAL_ROUTINE_COMPLETE)
                    .activityDate(day)
                    .createdAt(LocalDateTime.now(KST))
                    .isPublic(false) // 필요 시 정책에 맞게 조정
                    .build();
            userActivityRepository.save(ua);
        }

        // 커밋 이후 출석 체크 트리거
        events.publishEvent(new RoutineCompletedEvent(userId, routineId, day));
    }

    /** 완료 취소: 실행 로그 삭제 + 활동 레코드 삭제(카운트 ↓) */
    @Transactional
    public void undo(Integer userId, Integer routineId, LocalDate date) {
        // 실행 로그 삭제
        execRepo.findByUserIdAndRoutineIdAndExecDate(userId, routineId, date)
                .ifPresent(execRepo::delete);

        // >>> 활동 레코드 삭제 (카운트 ↓)
        userActivityRepository.deleteByUserIdAndPersonalRoutine_RoutineIdAndActivityTypeAndActivityDate(
                userId.longValue(), routineId, ActivityType.PERSONAL_ROUTINE_COMPLETE, date);

        // 엄격모드: 활동 레코드가 0이면 출석도 취소하는 추가 정책이 있으면 여기서 처리
    }
}
