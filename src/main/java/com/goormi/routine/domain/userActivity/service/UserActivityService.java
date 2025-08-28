package com.goormi.routine.domain.userActivity.service;

import com.goormi.routine.domain.userActivity.dto.UserActivityRequest;
import com.goormi.routine.domain.userActivity.dto.UserActivityResponse;

import java.time.LocalDate;
import java.util.List;

public interface UserActivityService {

    UserActivityResponse create(Long userId, UserActivityRequest request);

    UserActivityResponse updateActivity(Long userId, UserActivityRequest request);

    List<UserActivityResponse> getUserActivitiesPerDay(Long userId, LocalDate activityDate);
}
