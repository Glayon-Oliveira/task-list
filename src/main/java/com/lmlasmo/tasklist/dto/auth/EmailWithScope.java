package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailWithScope extends EmailDTO{

	@JsonProperty(required = false)
	private EmailConfirmationScope scope = EmailConfirmationScope.LINK;
	
}
