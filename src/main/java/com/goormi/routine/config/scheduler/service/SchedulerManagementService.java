package com.goormi.routine.config.scheduler.service;

import java.util.Map;

public interface SchedulerManagementService {
	void executeMonthlyReset();

	void retryFailedMessages(String monthYear);

	Map<String, Object> getSchedulerStatus();

	Map<String, Object> getFailedMessageStatus(String monthYear);
}
