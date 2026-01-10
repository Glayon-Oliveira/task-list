package com.lmlasmo.tasklist.doc;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().components(new Components().addSecuritySchemes(
				"bearerAuth",
				new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					));
	}

	@Bean
	public OperationCustomizer operationCustomizer() {
		return (operation, handlerMethod) -> {
			SimpleApiDoc doc = AnnotatedElementUtils.findMergedAnnotation(
					handlerMethod.getMethod(),
					SimpleApiDoc.class);
			
			if(doc == null) return operation;
			
			operation.setSummary(doc.summary());
			operation.setDescription(doc.description());
			
			for(StatusResponseApiDoc response: doc.success()) {
				String description = response.message().isBlank() ? HttpStatus.valueOf(response.status()).getReasonPhrase()
																  : response.message();
				
				buildResponse(operation, response.status(), description, false);
			}
			
			for(StatusResponseApiDoc response: doc.errors()) {
				String description = response.message().isBlank() ? HttpStatus.valueOf(response.status()).getReasonPhrase()
																  : response.message();
				
				buildResponse(operation, response.status(), description, true);
			}
			
			buildResponse(operation, 500, "Unexpected internal error", true);
			
			buildEtagSupport(operation, handlerMethod);
			
			if(AnnotatedElementUtils.hasMetaAnnotationTypes(handlerMethod.getMethod(), SecurityRequirement.class)) {
				buildResponse(operation, 401, "Unauthenticated user", true);
			}
			
			return operation;
		};
	}
	
	private void buildEtagSupport(Operation operation, HandlerMethod handlerMethod) {
		ETagSupport eTag = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), ETagSupport.class);

		if (eTag == null) return;

		String headerName = eTag.type() == ETagSupport.ETagType.IF_MATCH ? "If-Match" : "If-None-Match";

		operation.addParametersItem(new Parameter()
				.in("header")
				.name(headerName)
				.required(false)
				.description("ETag for version control")
				.schema(new StringSchema()));

		switch (eTag.type()) {
			case IF_NONE_MATCH: {
				operation.getResponses().addApiResponse("304", new ApiResponse().description("The operation failed because the resource version does not match the specified ETag"));
				break;
			}

			case IF_MATCH: {
				operation.getResponses().addApiResponse("412", new ApiResponse().description("The precondition failed: the provided ETag does not match the current version of the resource"));
			}
		}
	}
	
	private void buildResponse(Operation operation, int status, String description, boolean isException) {
		String statusStr = Integer.toString(status);
		
		if (operation.getResponses().containsKey(statusStr)) return;
		
		ApiResponse apiResponse = new ApiResponse()
				.description(description);
		
		if(isException) {
			apiResponse.content(new Content().addMediaType(
					"application/json",
					buildExceptionSchema()));
		}
		
		operation.getResponses().addApiResponse(
				statusStr,
				apiResponse);
	}
	
	@SuppressWarnings("unchecked")
	private MediaType buildExceptionSchema() {
	    Schema<Object> schema = new Schema<>()
	            .type("object")
	            .additionalProperties(true);
	    
	    schema.addProperty("timestamp",
	            new Schema<String>()
	                    .type("string")
	                    .format("date-time")
	                    .example("2025-01-01T12:00:00Z")
	    		);

	    schema.addProperty("status",
	            new Schema<Integer>().type("integer")
	    		);

	    schema.addProperty("error",
	            new Schema<String>().type("string")
	    		);

	    schema.addProperty("path",
	            new Schema<String>()
	                    .type("string")
	                    .example("/user/i")
	    		);
	    
	    MediaType mediaType = new MediaType();
	    mediaType.setSchema(schema);
	    
	    return mediaType;
	}
	
}
