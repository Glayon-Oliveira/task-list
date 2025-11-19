package com.lmlasmo.tasklist.security;

import java.util.function.BiFunction;

import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.service.ResourceAccessService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class AuthenticatedResourceAccess {
	
	private ResourceAccessService resourceAccessService;
	
	public Mono<Integer> canAccess(BiFunction<Integer, ResourceAccessService, Mono<Void>> access) {
		return Mono.defer(() -> 
			AuthenticatedTool.getUserId()
				.flatMap(usid -> {
					return access.apply(usid, resourceAccessService)
							.thenReturn(usid);
				})
		);
	}
	
}