package com.goormi.routine.domain.userActivity.service;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import com.goormi.routine.domain.group.repository.GroupRepository;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import com.goormi.routine.domain.userActivity.dto.UserActivityRequest;
import com.goormi.routine.domain.userActivity.dto.UserActivityResponse;
import com.goormi.routine.domain.userActivity.entity.UserActivity;
import com.goormi.routine.domain.userActivity.repository.UserActivityRepository;
import com.goormi.routine.personal_routines.domain.PersonalRoutine;
import com.goormi.routine.personal_routines.repository.PersonalRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService{
    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PersonalRoutineRepository personalRoutineRepository;

    @Override
    public UserActivityResponse create(Long userId, UserActivityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserActivity userActivity;

        if (request.getGroupId() != null && request.getPersonalRoutineId() != null) {
                throw new IllegalArgumentException("Select one Group or personal Routine");
        }
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));
            GroupMember groupMember = groupMemberRepository.findByGroupAndUser(group, user)
                    .orElseThrow(() -> new IllegalArgumentException("GroupMember not found"));

            userActivity = UserActivity.createActivity(user, groupMember);

        }
        else if (request.getPersonalRoutineId() != null) {
            PersonalRoutine personalRoutine = personalRoutineRepository.findById(request.getPersonalRoutineId())
                    .orElseThrow(() -> new IllegalArgumentException("Personal Routine not found"));
            userActivity = UserActivity.createActivity(user, personalRoutine);
        } else {
          throw new  IllegalArgumentException("Invalid request");
        }

        userActivityRepository.save(userActivity);
        return UserActivityResponse.from(userActivity);
    }

    @Override
    public UserActivityResponse updateActivity(Long userId, UserActivityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserActivity userActivity = userActivityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (!Objects.equals(user.getId(), userActivity.getUser().getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        userActivity.updateActivity(request.getActivityType());
        return UserActivityResponse.from(userActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getUserActivitiesPerDay(Long userId, LocalDate activityDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<UserActivity> activities = userActivityRepository.findAllByUserAndActivityDate(user, activityDate);
        return activities.stream()
                .map(UserActivityResponse::from).toList();
    }


}
