package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.calendar.client.KakaoCalendarClient;
import com.goormi.routine.domain.calendar.dto.CalendarResponse;
import com.goormi.routine.domain.calendar.dto.KakaoCalendarDto.*;
import com.goormi.routine.domain.calendar.entity.UserCalendar;
import com.goormi.routine.domain.calendar.exception.CalendarAlreadyConnectedException;
import com.goormi.routine.domain.calendar.exception.CalendarNotFoundException;
import com.goormi.routine.domain.calendar.exception.KakaoApiException;
import com.goormi.routine.domain.calendar.repository.CalendarRepository;
import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * CalendarService 단위 테스트
 * 외부 API 의존성을 Mock으로 대체하여 빠르고 안정적인 테스트 수행
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("캘린더 서비스 테스트")
class CalendarServiceTest {

    @Mock
    private KakaoCalendarClient kakaoCalendarClient;

    @Mock
    private KakaoTokenService kakaoTokenService;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @Test
    @DisplayName("사용자 캘린더 생성 성공")
    void createUserCalendar_Success() {
        // Given
        Long userId = 1L;
        String accessToken = "mock-access-token";
        String subCalendarId = "sub-calendar-123";
        
        User mockUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("테스트유저")
                .build();

        CreateSubCalendarResponse kakaoResponse = new CreateSubCalendarResponse(subCalendarId);

        // UserCalendar을 직접 생성하여 active를 true로 설정
        UserCalendar mockUserCalendar = UserCalendar.builder()
                .user(mockUser)
                .subCalendarId(subCalendarId)
                .calendarName("routine-it for group")
                .color(UserCalendar.CalendarColor.LIME)
                .reminderMinutes(10)
                .active(true)
                .build();

        // Mock 설정
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(calendarRepository.existsByUser(mockUser)).willReturn(false);
        given(kakaoTokenService.getKakaoAccessTokenByUserId(userId)).willReturn(accessToken);
        given(kakaoCalendarClient.createSubCalendar(eq(accessToken), any(CreateSubCalendarRequest.class)))
                .willReturn(kakaoResponse);
        given(calendarRepository.save(any(UserCalendar.class))).willReturn(mockUserCalendar);

        // When
        CalendarResponse response = calendarService.createUserCalendar(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.subCalendarId()).isEqualTo(subCalendarId);
        assertThat(response.calendarName()).isEqualTo("routine-it for group");
        assertThat(response.color()).isEqualTo(UserCalendar.CalendarColor.LIME);
        assertThat(response.reminderMinutes()).isEqualTo(10);
        assertThat(response.active()).isTrue();

        // Mock 호출 검증
        verify(userRepository).findById(userId);
        verify(calendarRepository).existsByUser(mockUser);
        verify(kakaoTokenService).getKakaoAccessTokenByUserId(userId);
        verify(kakaoCalendarClient).createSubCalendar(eq(accessToken), any(CreateSubCalendarRequest.class));
        verify(calendarRepository).save(any(UserCalendar.class));
    }

    @Test
    @DisplayName("이미 연결된 캘린더가 있을 때 예외 발생")
    void createUserCalendar_AlreadyConnected_ThrowsException() {
        // Given
        Long userId = 1L;
        User mockUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(calendarRepository.existsByUser(mockUser)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> calendarService.createUserCalendar(userId))
                .isInstanceOf(CalendarAlreadyConnectedException.class)
                .hasMessageContaining("이미 캘린더가 연동되어 있습니다");

        // 불필요한 API 호출이 없음을 확인
        verify(kakaoTokenService, never()).getKakaoAccessTokenByUserId(any());
        verify(kakaoCalendarClient, never()).createSubCalendar(any(), any());
    }

    @Test
    @DisplayName("사용자 캘린더 생성 - 카카오 API 실패")
    void createUserCalendar_KakaoApiFails_ThrowsException() {
        // Given
        Long userId = 1L;
        User mockUser = User.builder().id(userId).build();
        String accessToken = "mock-access-token";

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(calendarRepository.existsByUser(mockUser)).willReturn(false);
        given(kakaoTokenService.getKakaoAccessTokenByUserId(userId)).willReturn(accessToken);
        given(kakaoCalendarClient.createSubCalendar(eq(accessToken), any(CreateSubCalendarRequest.class)))
                .willThrow(new RuntimeException("카카오 API 오류"));

        // When & Then
        assertThatThrownBy(() -> calendarService.createUserCalendar(userId))
                .isInstanceOf(KakaoApiException.class)
                .hasMessageContaining("캘린더 생성에 실패했습니다");
    }

    @Test
    @DisplayName("그룹 일정 생성 성공")
    void createGroupSchedule_Success() {
        // Given
        Long userId = 1L;
        String accessToken = "mock-access-token";
        String subCalendarId = "sub-calendar-123";
        String eventId = "event-123";

        User mockUser = User.builder().id(userId).build();
        UserCalendar mockCalendar = UserCalendar.createUserCalendar(mockUser, subCalendarId);
        
        // alarmTime을 설정한 그룹 생성
        Group mockGroup = Group.builder()
                .groupName("테스트 그룹")
                .maxMembers(5)
                .alarmTime(LocalTime.of(9, 0)) // 09:00으로 설정
                .authDays("1111111") // 매일
                .description("테스트 그룹 설명")
                .build();

        CreateEventResponse kakaoResponse = new CreateEventResponse(eventId);

        // Mock 설정
        given(calendarRepository.findByUserIdAndActiveTrue(userId)).willReturn(Optional.of(mockCalendar));
        given(kakaoTokenService.getKakaoAccessTokenByUserId(userId)).willReturn(accessToken);
        given(kakaoCalendarClient.createEvent(eq(accessToken), eq(subCalendarId), any(CreateEventRequest.class)))
                .willReturn(kakaoResponse);

        // When
        String result = calendarService.createGroupSchedule(userId, mockGroup);

        // Then
        assertThat(result).isEqualTo(eventId);

        // Mock 호출 검증
        verify(calendarRepository).findByUserIdAndActiveTrue(userId);
        verify(kakaoTokenService).getKakaoAccessTokenByUserId(userId);
        verify(kakaoCalendarClient).createEvent(eq(accessToken), eq(subCalendarId), any(CreateEventRequest.class));
    }

    @Test
    @DisplayName("활성화된 캘린더가 없을 때 그룹 일정 생성 실패")
    void createGroupSchedule_NoActiveCalendar_ThrowsException() {
        // Given
        Long userId = 1L;
        Group mockGroup = Group.builder()
                .groupName("테스트그룹")
                .maxMembers(5)
                .alarmTime(LocalTime.of(9, 0))
                .authDays("1111111")
                .build();

        given(calendarRepository.findByUserIdAndActiveTrue(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> calendarService.createGroupSchedule(userId, mockGroup))
                .isInstanceOf(CalendarNotFoundException.class)
                .hasMessageContaining("활성화된 캘린더를 찾을 수 없습니다");

        // API 호출이 없음을 확인
        verify(kakaoTokenService, never()).getKakaoAccessTokenByUserId(any());
        verify(kakaoCalendarClient, never()).createEvent(any(), any(), any());
    }

    @Test
    @DisplayName("그룹 일정 삭제 성공")
    void deleteGroupSchedule_Success() {
        // Given
        String eventId = "event-123";
        Long userId = 1L;
        String accessToken = "mock-access-token";

        given(kakaoTokenService.getKakaoAccessTokenByUserId(userId)).willReturn(accessToken);

        // When
        assertThatCode(() -> calendarService.deleteGroupSchedule(eventId, userId))
                .doesNotThrowAnyException();

        // Then
        verify(kakaoTokenService).getKakaoAccessTokenByUserId(userId);
    }

    @Test
    @DisplayName("사용자 캘린더 삭제 성공")
    void deleteUserCalendar_Success() {
        // Given
        Long userId = 1L;
        String subCalendarId = "sub-calendar-123";
        String accessToken = "mock-access-token";

        User mockUser = User.builder().id(userId).build();
        UserCalendar mockCalendar = UserCalendar.createUserCalendar(mockUser, subCalendarId);

        given(calendarRepository.findByUserId(userId)).willReturn(Optional.of(mockCalendar));
        given(kakaoTokenService.getKakaoAccessTokenByUserId(userId)).willReturn(accessToken);

        // When
        assertThatCode(() -> calendarService.deleteUserCalendar(userId))
                .doesNotThrowAnyException();

        // Then
        verify(calendarRepository).findByUserId(userId);
        verify(kakaoTokenService).getKakaoAccessTokenByUserId(userId);
        verify(kakaoCalendarClient).deleteSubCalendar(accessToken, subCalendarId);
        verify(calendarRepository).delete(mockCalendar);
    }

    @Test
    @DisplayName("삭제할 캘린더가 없을 때 정상 처리")
    void deleteUserCalendar_NoCalendar_HandledGracefully() {
        // Given
        Long userId = 1L;

        given(calendarRepository.findByUserId(userId)).willReturn(Optional.empty());

        // When
        assertThatCode(() -> calendarService.deleteUserCalendar(userId))
                .doesNotThrowAnyException();

        // Then
        verify(calendarRepository).findByUserId(userId);
        // 캘린더가 없으면 API 호출하지 않음
        verify(kakaoTokenService, never()).getKakaoAccessTokenByUserId(any());
        verify(kakaoCalendarClient, never()).deleteSubCalendar(any(), any());
    }

    @Test
    @DisplayName("그룹 일정 수정 성공")
    void updateGroupSchedule_Success() {
        // Given
        Long userId = 1L;
        String eventId = "event-123";
        String accessToken = "mock-access-token";
        
        // alarmTime과 authDays를 설정한 그룹 생성
        Group mockGroup = Group.builder()
                .groupName("수정된 그룹")
                .maxMembers(5)
                .alarmTime(LocalTime.of(10, 30)) // 10:30으로 설정
                .authDays("1010101") // 월수금
                .description("수정된 그룹 설명")
                .build();

        given(kakaoTokenService.getKakaoAccessTokenByUserId(userId)).willReturn(accessToken);

        // When
        assertThatCode(() -> calendarService.updateGroupSchedule(userId, mockGroup, eventId))
                .doesNotThrowAnyException();

        // Then
        verify(kakaoTokenService).getKakaoAccessTokenByUserId(userId);
        verify(kakaoCalendarClient).updateEvent(eq(accessToken), eq(eventId), any(UpdateEventRequest.class));
    }
}
