package com.lmlasmo.tasklist.scheduled;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.service.UserService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class UserCleanup {

	@NonNull private UserService userService;
	
	@Value("${app.user.expires}")
	private Duration expires;
	
	@Scheduled(cron = "0 0 3 1 */7 *", zone = "UTC")
    public void taskMarkUsersInactive() {
		userService.markUsersInactive(expires);
    }

    @Scheduled(cron = "0 0 4 1 */3 *", zone = "UTC")
    public void taskMarkInactiveForDeletion() {
    	userService.markInactiveUsersForDeletion();
    }

    @Scheduled(cron = "0 0 5 1 */6 *", zone = "UTC")
    public void taskDeleteUsersMarked() {
    	userService.deleteUsersMarkedForDeletion();
    }
	
}
