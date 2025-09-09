package com.goormi.routine.domain.userActivity.service;

import com.goormi.routine.domain.userActivity.dto.UserActivityRequest;
import com.goormi.routine.domain.userActivity.dto.UserActivityResponse;

import java.time.LocalDate;
import java.util.List;

public interface UserActivityService {

    UserActivityResponse create(Long userId, UserActivityRequest request);

    UserActivityResponse updateActivity(Long userId, UserActivityRequest request);

    List<UserActivityResponse> getUserActivitiesPerDay(Long userId, LocalDate activityDate);

    List<UserActivityResponse> getImagesOfUserActivities(Long currentUserIdm, Long targetUserId);


    //출석
    int getConsecutiveAttendanceDays(Long userId, LocalDate today);

    boolean hasAttendanceOn(Long userId, LocalDate date);
}
