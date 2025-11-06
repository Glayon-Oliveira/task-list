package com.lmlasmo.tasklist.controller.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.AccountController;
import com.lmlasmo.tasklist.dto.UserEmailDTO;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailService;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.UserEmailService;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

import jakarta.persistence.EntityExistsException;

@WebMvcTest(controllers = {AccountController.class})
@Import(EmailConfirmationService.class)
@TestInstance(Lifecycle.PER_CLASS)
public class AccountControllerTest extends AbstractControllerTest {
	
	@MockitoBean
	private UserEmailService userEmailService;
	
	@MockitoBean("resourceAccess")
	private ResourceAccessService resourceAccessService;
	
	@MockitoBean
	private EmailService emailService;
	
	@Autowired
	private EmailConfirmationService confirmationService;

	@RepeatedTest(2)
	public void linkEmail(RepetitionInfo info) throws Exception {
		String emailFormat = """
				{
						"email": "%s",
						"confirmation": {
							 "code": "%s",
							 "hash": "%s",
							 "timestamp": "%s"
						}
				}
			""";
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash("test@example.com");
		
		int currentRept = info.getCurrentRepetition();

		String email = String.format(
				emailFormat,
				currentRept % 2 == 0 ? "test@example.com" : "testexample.com",
				codeHash.getCode(),
				codeHash.getHash(),
				codeHash.getTimestamp());
		
		String baseUri = "/api/account/email/link";

		when(userEmailService.save("test@example.com", getDefaultUser().getId())).thenReturn(new UserEmailDTO(new UserEmail("test@example.com")));
		when(getUserService().save(any())).thenThrow(EntityExistsException.class);
		when(userEmailService.existsByEmail("test@example.com")).thenReturn(false);		

		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(email))
		.andExpect(MockMvcResultMatchers.status().is(currentRept % 2 == 0 ? 204 : 400))
		.andExpect(result -> VerifyResolvedException.verify(result, currentRept % 2 == 0 ? null : MethodArgumentNotValidException.class));
	}
	
	@RepeatedTest(2)
	public void terminateEmail(RepetitionInfo info) throws Exception {
		String emailFormat = """
				{
						"email": "%s"
				}
			""";
		
		int currentRept = info.getCurrentRepetition();

		String email = String.format(emailFormat, currentRept % 2 == 0 ? "test@example.com" : "testexample.com");
		String baseUri = "/api/account/email/terminate";

		when(userEmailService.save("test@example.com", getDefaultUser().getId())).thenReturn(new UserEmailDTO(new UserEmail("test@example.com")));
		when(resourceAccessService.canAccessEmail("test@example.com", getDefaultUser().getId())).thenReturn(true);

		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(email))
		.andExpect(MockMvcResultMatchers.status().is(currentRept % 2 == 0 ? 204 : 400))
		.andExpect(result -> VerifyResolvedException.verify(result, currentRept % 2 == 0 ? null : MethodArgumentNotValidException.class));
	}
	
}
