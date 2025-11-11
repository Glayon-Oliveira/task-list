package com.lmlasmo.tasklist.security;

import java.util.Collection;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import reactor.core.publisher.Mono;

public class AuthenticatedTool {
	
	public static Mono<Integer> getUserId() {
		return Mono.defer(ReactiveSecurityContextHolder::getContext)
				.map(cx -> Integer.parseInt(cx.getAuthentication().getPrincipal().toString()))
				.onErrorResume(e -> Mono.error(new AccessDeniedException("Access denied")))
				.filter(id -> id != null && id > 0)
				.switchIfEmpty(Mono.error(new AccessDeniedException("Access denied")));
	}
	
	public static Mono<Collection<?extends GrantedAuthority>> getRoles(){
		return Mono.defer(ReactiveSecurityContextHolder::getContext)
				.map(cx -> cx.getAuthentication().getAuthorities());
	}

}
