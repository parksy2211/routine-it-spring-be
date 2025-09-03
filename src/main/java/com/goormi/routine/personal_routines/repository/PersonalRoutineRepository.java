package com.goormi.routine.personal_routines.repository;

import com.goormi.routine.personal_routines.domain.PersonalRoutine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface PersonalRoutineRepository extends JpaRepository<PersonalRoutine, Integer> {

    List<PersonalRoutine> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer userId);

    Optional<PersonalRoutine> findByRoutineIdAndIsDeletedFalse(Integer routineId);

    boolean existsByUserIdAndRoutineNameAndIsDeletedFalse(Integer userId, String routineName);

    List<PersonalRoutine> findByIsDeletedFalseAndIsAlarmOnTrueAndStartTime(LocalTime startTime);
}
