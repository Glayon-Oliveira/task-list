package com.lmlasmo.tasklist.dto.auth;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfirmationHashDTO {
	
	private String hash;
	
	private Instant timestamp;	
	
}
