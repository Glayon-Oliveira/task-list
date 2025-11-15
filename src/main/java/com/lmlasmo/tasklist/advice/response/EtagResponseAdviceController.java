package com.lmlasmo.tasklist.advice.response;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.lmlasmo.tasklist.dto.VersionedDTO;

@RestControllerAdvice
public class EtagResponseAdviceController implements ResponseBodyAdvice<Object>{

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		
		long version = extractVersion(body);
		
		if(version >= 0) response.getHeaders().setETag(Long.toString(version));
		
		return body;
	}
	
	private long extractVersion(Object body) {
		if (body instanceof VersionedDTO dto) return dto.getVersion();
	    if (body instanceof ResponseEntity<?> entity && entity.getBody() instanceof VersionedDTO dto) return dto.getVersion();
	    
	    if(body instanceof List<?> list) {
	    	return list.stream()
	    			.filter(o -> o instanceof VersionedDTO)
	    			.mapToLong(o -> ((VersionedDTO) o).getVersion())
	    			.reduce((a, b) -> a+b)
	    			.orElse(-1);
	    }
	    
	    return -1;
	}	

}
