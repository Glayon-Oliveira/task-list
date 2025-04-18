package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.dto.JWTTokenDTO;
import com.lmlasmo.tasklist.dto.LoginDTO;
import com.lmlasmo.tasklist.dto.SignupDTO;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.service.JwtService;
import com.lmlasmo.tasklist.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/sign")
public class SignController {
	
	private UserService userService;
	private JwtService jwtService;
	private AuthenticationManager manager;	
	
	@PostMapping("/in")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public JWTTokenDTO inByJson(@RequestBody @Valid LoginDTO login) {		
		Authentication auth = new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());		
		auth = manager.authenticate(auth);
		
		User user = (User) auth.getDetails();		
		String[] roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);		
		String token = jwtService.gerateToken(user.getId(), roles);		
		return new JWTTokenDTO(token);
	}
	
	@PostMapping("/in")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public JWTTokenDTO inByForm(@ModelAttribute @Valid LoginDTO login) {
		return inByJson(login);
	}
	
	@PostMapping("/up")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public UserDTO upByJson(@RequestBody @Valid SignupDTO signup) {
		return userService.save(signup);		
	}
	
	@PostMapping("/up")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public UserDTO upByForm(@ModelAttribute @Valid SignupDTO signup) {
		return upByJson(signup);
	}

}
