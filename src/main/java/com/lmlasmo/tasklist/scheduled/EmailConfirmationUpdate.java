package com.lmlasmo.tasklist.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.service.EmailConfirmationService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class EmailConfirmationUpdate {

	private EmailConfirmationService confirmationService;
	
	@Scheduled(cron = "0 0 0 1 * *" )
	public void update() {
		confirmationService.update();
	}
	
}
