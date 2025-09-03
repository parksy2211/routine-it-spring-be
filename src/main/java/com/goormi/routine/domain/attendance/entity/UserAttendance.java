package com.goormi.routine.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_attendance",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_date", columnNames = {"user_id", "check_date"}),
        indexes = { @Index(name = "idx_user_month", columnList = "user_id, check_date") }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAttendance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;                  // Principal과 타입 맞춤(Long)

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;          // KST 기준 '그 날'

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;      // 체크 실제 시각(KST 저장)

    private String device;
    private String ip;
}
