package com.goormi.routine.domain.userActivity.service;

import com.goormi.routine.domain.group.dto.request.GroupCreateRequest;
import com.goormi.routine.domain.group.dto.response.GroupResponse;
import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupType;
import com.goormi.routine.domain.group.repository.GroupRepository;
import com.goormi.routine.domain.group.service.GroupService;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import com.goormi.routine.domain.userActivity.dto.UserActivityRequest;
import com.goormi.routine.domain.userActivity.dto.UserActivityResponse;
import com.goormi.routine.domain.userActivity.entity.ActivityType;
import com.goormi.routine.domain.personal_routines.domain.PersonalRoutine;
import com.goormi.routine.domain.personal_routines.dto.PersonalRoutineRequest;
import com.goormi.routine.domain.personal_routines.dto.PersonalRoutineResponse;
import com.goormi.routine.domain.personal_routines.repository.PersonalRoutineRepository;
import com.goormi.routine.domain.personal_routines.service.PersonalRoutineService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("ci")
@Transactional
class UserActivityServiceTest {

    @Autowired
    private UserActivityService userActivityService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupService groupService;
    @Autowired
    private PersonalRoutineService personalRoutineService;
    @Autowired
    private PersonalRoutineRepository personalRoutineRepository;

    private User leader;
    private User user;
    private Group savedGroup;
    private PersonalRoutine savedRoutine;

    @BeforeEach
    void setUp() {
        // 테스트용 리더 생성
        leader = User.builder()
                .kakaoId("leader")
                .email("testLeader@kakao.com")
                .nickname("testLeader")
                .build();
        userRepository.save(leader);

        // 테스트용 일반 유저 생성
        user = User.builder()
                .kakaoId("user")
                .email("testUser@kakao.com")
                .nickname("testUser")
                .build();
        userRepository.save(user);

        // 테스트용 그룹 생성
        GroupCreateRequest groupCreateRequest = GroupCreateRequest.builder()
                .groupName("testGroup")
                .groupType(GroupType.FREE)
                .maxMembers(5)
                .build();
        GroupResponse groupResponse = groupService.createGroup(leader.getId(), groupCreateRequest);
        savedGroup = groupRepository.findById(groupResponse.getGroupId()).orElseThrow();

        // 테스트용 개인 루틴 생성
        PersonalRoutineRequest routineRequest = PersonalRoutineRequest.builder()
                .userId(user.getId().intValue())
                .routineName("testRoutine")
                .startTime(LocalTime.now())
                .repeatDays("1111111")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build();
        PersonalRoutineResponse routineResponse = personalRoutineService.create(routineRequest);
        savedRoutine = personalRoutineRepository.findById(routineResponse.getRoutineId()).orElseThrow();
    }

    @Test
    @DisplayName("그룹 활동 생성 성공")
    void create_group_activity_success() {
        // given
        UserActivityRequest request = UserActivityRequest.builder()
                .activityType(ActivityType.GROUP_AUTH_COMPLETE)
                .activityDate(LocalDate.now())
                .groupId(savedGroup.getGroupId())
                .build();

        // when
        UserActivityResponse response = userActivityService.create(leader.getId(), request);

        // then
        assertThat(response.getUserId()).isEqualTo(leader.getId());
        assertThat(response.getGroupId()).isEqualTo(savedGroup.getGroupId());
        assertThat(response.getActivityType()).isEqualTo(ActivityType.GROUP_AUTH_COMPLETE);
    }

    @Test
    @DisplayName("개인 루틴 활동 생성 성공")
    void create_personal_routine_activity_success() {
        // given
        UserActivityRequest request = UserActivityRequest.builder()
                .activityType(ActivityType.PERSONAL_ROUTINE_COMPLETE)
                .activityDate(LocalDate.now())
                .personalRoutineId(savedRoutine.getRoutineId())
                .build();

        // when
        UserActivityResponse response = userActivityService.create(leader.getId(), request);

        // then
        assertThat(response.getUserId()).isEqualTo(leader.getId());
        assertThat(response.getPersonalRoutineId()).isEqualTo(savedRoutine.getRoutineId());
        assertThat(response.getActivityType()).isEqualTo(ActivityType.PERSONAL_ROUTINE_COMPLETE);
    }

    @Test
    @DisplayName("활동 생성 실패 - 그룹 ID와 개인 루틴 ID 동시 요청")
    void create_activity_fail_with_both_ids() {
        // given
        UserActivityRequest request = UserActivityRequest.builder()
                .activityType(ActivityType.PERSONAL_ROUTINE_COMPLETE)
                .activityDate(LocalDate.now())
                .personalRoutineId(savedRoutine.getRoutineId())
                .groupId(savedGroup.getGroupId())
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userActivityService.create(user.getId(), request));
        assertThat(exception.getMessage()).isEqualTo("Select one Group or personal Routine");
    }

    @Test
    @DisplayName("활동 생성 실패 - 유효하지 않은 요청")
    void create_activity_fail_with_invalid_request() {
        // given
        UserActivityRequest request = UserActivityRequest.builder()
                .activityType(ActivityType.PERSONAL_ROUTINE_COMPLETE)
                .activityDate(LocalDate.now())
                .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userActivityService.create(user.getId(), request));
        assertThat(exception.getMessage()).isEqualTo("Invalid request");
    }
}