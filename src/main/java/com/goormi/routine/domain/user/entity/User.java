package com.goormi.routine.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String kakaoId;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_image_url", columnDefinition = "TEXT", length = 255)
    private String profileImageUrl;

    @Column(name = "profile_message", columnDefinition = "TEXT")
    private String profileMessage;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(
        name = "is_alarm_on",
        nullable = false,
        columnDefinition = "BOOLEAN DEFAULT TRUE"
    )
    @Builder.Default
    private Boolean isAlarmOn = true;

    @Column(
        name = "is_dark_mode",
        nullable = false,
        columnDefinition = "BOOLEAN DEFAULT FALSE"
    )
    @Builder.Default
    private Boolean isDarkMode = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void createProfile(String nickname, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
    
    public void updateProfile(String nickname, String profileMessage, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
    
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public static User createKakaoUser(String kakaoId, String email, String nickname, String profileImageUrl) {
        return User.builder()
                .kakaoId(kakaoId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(UserRole.USER)
                .build();
    }

    public void updateSettings(Boolean isAlarmOn, Boolean isDarkMode) {
        if (isAlarmOn != null) this.isAlarmOn = isAlarmOn;
        if (isDarkMode != null) this.isDarkMode = isDarkMode;
    }

    public enum UserRole {
        USER, ADMIN
    }
}