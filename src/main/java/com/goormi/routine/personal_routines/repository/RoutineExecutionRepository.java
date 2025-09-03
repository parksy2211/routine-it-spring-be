package com.goormi.routine.personal_routines.repository;

import com.goormi.routine.personal_routines.domain.RoutineExecution;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface RoutineExecutionRepository extends JpaRepository<RoutineExecution, Long> {

    Optional<RoutineExecution> findByUserIdAndRoutineIdAndExecDate(
            Integer userId, Integer routineId, LocalDate execDate);

    @Query("select count(re) from RoutineExecution re " +
            "where re.userId=:userId and re.execDate=:execDate")
    long countByUserAndDate(@Param("userId") Integer userId,
                            @Param("execDate") LocalDate execDate);
}
