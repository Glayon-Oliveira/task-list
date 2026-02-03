package com.lmlasmo.tasklist.web.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import com.lmlasmo.tasklist.web.util.ETagHelper;

import reactor.core.publisher.Mono;

public class ETagHelperArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(ETagHelper.class);
	}

	@Override
	public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext,
			ServerWebExchange exchange) {
		
		return Mono.just(new ETagHelper(exchange));
	}

}
