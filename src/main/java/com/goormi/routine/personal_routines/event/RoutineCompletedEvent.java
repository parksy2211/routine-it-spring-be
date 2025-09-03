package com.goormi.routine.personal_routines.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class RoutineCompletedEvent {
    private final Integer userId;
    private final Integer routineId;
    private final LocalDate execDate; // KST 기준 날짜
}
