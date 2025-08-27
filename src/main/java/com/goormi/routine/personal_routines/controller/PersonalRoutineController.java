package com.goormi.routine.personal_routines.controller;

import com.goormi.routine.personal_routines.dto.PersonalRoutineRequest;
import com.goormi.routine.personal_routines.dto.PersonalRoutineResponse;
import com.goormi.routine.personal_routines.dto.PersonalRoutineUpdateRequest;
import com.goormi.routine.personal_routines.service.PersonalRoutineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-routines")
public class PersonalRoutineController {

    private final PersonalRoutineService service;

    @PostMapping
    public ResponseEntity<PersonalRoutineResponse> create(@Valid @RequestBody PersonalRoutineRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PersonalRoutineResponse>> listByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(service.listByUser(userId));
    }

    @GetMapping("/{routineId}")
    public ResponseEntity<PersonalRoutineResponse> get(@PathVariable Integer routineId) {
        return ResponseEntity.ok(service.get(routineId));
    }

    @PatchMapping("/{routineId}")
    public ResponseEntity<PersonalRoutineResponse> update(@PathVariable Integer routineId,
                                                          @Valid @RequestBody PersonalRoutineUpdateRequest req) {
        return ResponseEntity.ok(service.update(routineId, req));
    }

    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> delete(@PathVariable Integer routineId) {
        service.softDelete(routineId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{routineId}/toggle-alarm")
    public ResponseEntity<PersonalRoutineResponse> toggleAlarm(@PathVariable Integer routineId) {
        return ResponseEntity.ok(service.toggleAlarm(routineId));
    }

    @PostMapping("/{routineId}/toggle-public")
    public ResponseEntity<PersonalRoutineResponse> togglePublic(@PathVariable Integer routineId) {
        return ResponseEntity.ok(service.togglePublic(routineId));
    }
}
