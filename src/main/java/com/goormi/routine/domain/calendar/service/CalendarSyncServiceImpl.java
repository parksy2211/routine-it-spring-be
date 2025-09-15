package com.goormi.routine.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarSyncServiceImpl implements CalendarSyncService {

    private final CalendarService calendarService;

    @Async
    @Override
    public void syncUserCalendar(Long userId) {
        log.info("Start calendar sync for user: {}", userId);
        try {
            // 멱등성이 보장된 createUserCalendar 호출
            calendarService.createUserCalendar(userId);
            log.info("Finished calendar sync for user: {}", userId);
        } catch (Exception e) {
            log.error("An unexpected error occurred during calendar sync for user {}", userId, e);
        }
    }
}
