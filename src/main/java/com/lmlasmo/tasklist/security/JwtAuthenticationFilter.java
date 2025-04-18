package com.lmlasmo.tasklist.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.lmlasmo.tasklist.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private JwtService service;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {		
		String token = getToken(request);
		
		if(token != null) authenticate(token);
		
		filterChain.doFilter(request, response);
	}
	
	private void authenticate(String token) {		
		int id = service.getId(token);
		List<SimpleGrantedAuthority> roles = List.of(service.getRoles(token)).stream()
				.map(SimpleGrantedAuthority::new)
				.toList();
		
		UsernamePasswordAuthenticationToken auth = UsernamePasswordAuthenticationToken.authenticated(id, null, roles);
		SecurityContextHolder.getContext().setAuthentication(auth);		
	}
	
	private String getToken(HttpServletRequest request) {		
		String auth = request.getHeader("Authorization");
		
		if(auth == null) return null;
		
		if(!auth.startsWith("Bearer")) return null;
		
		auth = auth.replace("Bearer", "");
		auth = auth.replace(" ", "");		
		return auth;
	}
	
}
