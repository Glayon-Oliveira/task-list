package com.lmlasmo.tasklist.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lmlasmo.tasklist.model.User;

public abstract class AuthenticatedTool {
	
	public static User getUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
	}
	
	public static int getUserId() {
		return (int) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	public static Collection<?extends GrantedAuthority> getRoles(){
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	}

}
