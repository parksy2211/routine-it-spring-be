package com.goormi.routine.personal_routines.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "routine_execution",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_routine_date",
                columnNames = {"user_id", "routine_id", "exec_date"}
        ),
        indexes = {
                @Index(name = "idx_user_date", columnList = "user_id, exec_date")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoutineExecution {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "routine_id", nullable = false)
    private Integer routineId;

    /** 완료 처리된 날 (KST 기준 날짜) */
    @Column(name = "exec_date", nullable = false)
    private LocalDate execDate;

    /** 실제 완료 시각 (KST 저장) */
    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;
}
