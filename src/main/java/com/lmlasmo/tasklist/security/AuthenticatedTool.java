package com.lmlasmo.tasklist.security;

import java.util.Collection;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authTool")
public class AuthenticatedTool {
	
	public int getUserId() {
		return DirectAuthenticatedTool.getUserId();
	}
	
	public static Collection<?extends GrantedAuthority> getRoles(){
		return DirectAuthenticatedTool.getRoles();
	}

	
	public interface DirectAuthenticatedTool {
		public static int getUserId() {
			try {
				int id = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
				
				if(id > 0) return id;
			}catch(Exception e) {}
			
			throw new AccessDeniedException("Access denied");
		}
		
		public static Collection<?extends GrantedAuthority> getRoles(){
			return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		}
	}	

}
