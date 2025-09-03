package com.goormi.routine.domain.scheduler.service;

import java.util.Map;

public interface SchedulerManagementService {
	void executeMonthlyReset();

	void retryFailedMessages(String monthYear);

	Map<String, Object> getSchedulerStatus();

	Map<String, Object> getFailedMessageStatus(String monthYear);
}
