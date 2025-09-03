// src/main/java/com/goormi/routine/personal_routines/support/RRuleUtil.java
package com.goormi.routine.personal_routines.support;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class RRuleUtil {

    private static final String[] BYDAY = {"MO","TU","WE","TH","FR","SA","SU"};
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter UNTIL_FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    /** "1111100" => "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;UNTIL=20250930T145959Z" */
    public static String toWeeklyRRule(String repeatDays, LocalDate endDateOrNull) {
        if (repeatDays == null || repeatDays.length() != 7) throw new IllegalArgumentException("repeatDays invalid");
        StringBuilder days = new StringBuilder();
        for (int i = 0; i < 7; i++) if (repeatDays.charAt(i) == '1') {
            if (days.length() > 0) days.append(",");
            days.append(BYDAY[i]);
        }
        String base = "FREQ=WEEKLY;BYDAY=" + days;
        if (endDateOrNull != null) {
            ZonedDateTime untilUtc = endDateOrNull.atTime(LocalTime.of(23,59,59))
                    .atZone(KST).withZoneSameInstant(ZoneOffset.UTC);
            base += ";UNTIL=" + untilUtc.format(UNTIL_FMT);
        }
        return base;
    }

    /** startDate + startTime을 KST로 ZonedDateTime */
    public static ZonedDateTime zdtKst(LocalDate d, LocalTime t) {
        return ZonedDateTime.of(d, t, KST);
    }
}
