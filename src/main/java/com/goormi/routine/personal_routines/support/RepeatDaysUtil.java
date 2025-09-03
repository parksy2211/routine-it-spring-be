// src/main/java/com/goormi/routine/personal_routines/support/RepeatDaysUtil.java
package com.goormi.routine.personal_routines.support;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class RepeatDaysUtil {
    private RepeatDaysUtil() {}

    /**
     * repeatDays: 월~일 7자리(예: 1010100)
     */
    public static boolean matches(LocalDate date, String repeatDays) {
        if (repeatDays == null || repeatDays.length() != 7) return false;
        // 월(0)~일(6)로 맵핑
        int idx = switch (date.getDayOfWeek()) {
            case MONDAY -> 0;
            case TUESDAY -> 1;
            case WEDNESDAY -> 2;
            case THURSDAY -> 3;
            case FRIDAY -> 4;
            case SATURDAY -> 5;
            case SUNDAY -> 6;
        };
        return repeatDays.charAt(idx) == '1';
    }
}
