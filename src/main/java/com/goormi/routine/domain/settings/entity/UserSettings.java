package com.goormi.routine.domain.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, unique = true)
	private Long userId;

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

	public void updateAlarmSetting(Boolean isAlarmOn) {
		if (isAlarmOn != null) {
			this.isAlarmOn = isAlarmOn;
		}
	}

	public void updateDarkModeSetting(Boolean isDarkMode) {
		if (isDarkMode != null) {
			this.isDarkMode = isDarkMode;
		}
	}

	public void updateSettings(Boolean isAlarmOn, Boolean isDarkMode) {
		updateAlarmSetting(isAlarmOn);
		updateDarkModeSetting(isDarkMode);
	}

	public static UserSettings createDefault(Long userId) {
		return UserSettings.builder()
			.userId(userId)
			.isAlarmOn(true)
			.isDarkMode(false)
			.build();
	}
}