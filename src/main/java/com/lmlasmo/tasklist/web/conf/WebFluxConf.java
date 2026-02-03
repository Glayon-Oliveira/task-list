package com.lmlasmo.tasklist.web.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import com.lmlasmo.tasklist.web.resolver.ETagHelperArgumentResolver;

@Configuration
public class WebFluxConf implements WebFluxConfigurer {

	@Override
	public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
		configurer.addCustomResolver(
				new ReactivePageableHandlerMethodArgumentResolver(),
				new ETagHelperArgumentResolver()
				);
	}
	
}
