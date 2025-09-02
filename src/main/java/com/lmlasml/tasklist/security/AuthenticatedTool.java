package com.lmlasml.tasklist.security;

import java.util.Collection;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public abstract class AuthenticatedTool {	
	
	public static int getUserId() {		
		try {
			int id = (int) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			
			if(id > 0) return id;
		}catch(Exception e) {}
		
		throw new AccessDeniedException("Access denied");
	}
	
	public static Collection<?extends GrantedAuthority> getRoles(){
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	}

}
