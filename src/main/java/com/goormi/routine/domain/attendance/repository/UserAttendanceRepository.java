package com.goormi.routine.domain.attendance.repository;

import com.goormi.routine.domain.attendance.entity.UserAttendance;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserAttendanceRepository extends JpaRepository<UserAttendance, Long> {

    Optional<UserAttendance> findByUserIdAndCheckDate(Long userId, LocalDate checkDate);

    long countByUserIdAndCheckDate(Long userId, LocalDate checkDate);

    @Query("select ua from UserAttendance ua " +
            "where ua.userId=:userId and ua.checkDate between :start and :end " +
            "order by ua.checkDate asc")
    List<UserAttendance> findByUserIdAndCheckDateBetween(@Param("userId") Long userId,
                                                         @Param("start") LocalDate start,
                                                         @Param("end") LocalDate end);

    @Query("select ua.checkDate from UserAttendance ua " +
            "where ua.userId=:userId order by ua.checkDate asc")
    List<LocalDate> findAllDatesByUserId(@Param("userId") Long userId);
}
