package com.goormi.routine.config.scheduler.service;

public interface MonthlySchedulerService {
	void executeMonthlyReset();
	void retryFailedReviewMessages();

	void manualMonthlyReset();

	void manualRetryReviewMessages(String monthYear);
}
