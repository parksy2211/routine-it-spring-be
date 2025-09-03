package com.goormi.routine.domain.scheduler.service;

public interface MonthlySchedulerService {
	void executeMonthlyReset();
	void retryFailedReviewMessages();

	void manualMonthlyReset();

	void manualRetryReviewMessages(String monthYear);
}
