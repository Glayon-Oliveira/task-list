package com.lmlasmo.tasklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountDTO {
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private long total;
	
}
