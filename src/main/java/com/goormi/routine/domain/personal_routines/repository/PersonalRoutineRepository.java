package com.goormi.routine.domain.personal_routines.repository;

import com.goormi.routine.domain.personal_routines.domain.PersonalRoutine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalRoutineRepository extends JpaRepository<PersonalRoutine, Integer> {

    Page<PersonalRoutine> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer userId,
            Pageable pageable
    );
    Optional<PersonalRoutine> findByRoutineIdAndIsDeletedFalse(Integer routineId);

    boolean existsByUserIdAndRoutineNameAndIsDeletedFalse(Integer userId, String routineName);
}
