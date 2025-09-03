// src/main/java/com/goormi/routine/personal_routines/controller/PersonalRoutineSyncController.java
package com.goormi.routine.personal_routines.controller;

import com.goormi.routine.personal_routines.service.AlarmSyncService;
import com.goormi.routine.personal_routines.repository.PersonalRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-routines")
public class PersonalRoutineSyncController {

    private final PersonalRoutineRepository repository;
    private final AlarmSyncService alarmSyncService;

    @PostMapping("/{routineId}/resync-alarm")
    public ResponseEntity<Void> resync(@PathVariable Integer routineId) {
        var e = repository.findByRoutineIdAndIsDeletedFalse(routineId)
                .orElseThrow(() -> new IllegalArgumentException("루틴을 찾을 수 없습니다."));
        alarmSyncService.sync(e);
        return ResponseEntity.noContent().build();
    }
}
