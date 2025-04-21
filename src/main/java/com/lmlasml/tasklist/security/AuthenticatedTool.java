package com.lmlasml.tasklist.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public abstract class AuthenticatedTool {	
	
	public static int getUserId() {
		return (int) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	public static Collection<?extends GrantedAuthority> getRoles(){
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	}

}
