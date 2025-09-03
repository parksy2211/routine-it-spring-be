package com.goormi.routine.personal_routines.event;

import com.goormi.routine.domain.attendance.service.AttendanceService;
import com.goormi.routine.personal_routines.repository.RoutineExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RoutineCompletedListener {

    private final RoutineExecutionRepository execRepo;
    private final AttendanceService attendanceService;

    /** 트랜잭션 커밋 후: 그 날 하나라도 완료했다면 출석 체크(멱등) */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompleted(RoutineCompletedEvent e) {
        if (execRepo.countByUserAndDate(e.getUserId(), e.getExecDate()) > 0) {
            attendanceService.checkIn(e.getUserId().longValue(), "routine", null);
        }
    }
}
