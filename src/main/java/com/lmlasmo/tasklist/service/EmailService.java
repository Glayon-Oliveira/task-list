package com.lmlasmo.tasklist.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class EmailService {

	private JavaMailSender mailSender;
	
	public Mono<Void> send(String to, String subject, String body) {		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		
		return Mono.fromRunnable(() -> mailSender.send(message));		
	}
	
}
