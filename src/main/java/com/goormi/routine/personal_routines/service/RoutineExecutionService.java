package com.goormi.routine.personal_routines.service;

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

        execRepo.findByUserIdAndRoutineIdAndExecDate(userId, routineId, day)
                .orElseGet(() -> execRepo.save(
                        RoutineExecution.builder()
                                .userId(userId)
                                .routineId(routineId)
                                .execDate(day)
                                .executedAt(LocalDateTime.now(KST))
                                .build()
                ));

        // 커밋 이후 출석 체크 트리거
        events.publishEvent(new RoutineCompletedEvent(userId, routineId, day));
    }

    /** 완료 취소: 로그만 삭제(기본 정책은 출석 유지) */
    @Transactional
    public void undo(Integer userId, Integer routineId, LocalDate date) {
        execRepo.findByUserIdAndRoutineIdAndExecDate(userId, routineId, date)
                .ifPresent(execRepo::delete);
        // 엄격모드: 해당 날짜의 실행 로그가 0이면 출석도 취소하는 로직을 여기서 추가 가능
    }
}
