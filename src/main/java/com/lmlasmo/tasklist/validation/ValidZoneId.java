package com.lmlasmo.tasklist.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ZoneIdValidator.class)
public @interface ValidZoneId {
	 String message() default "Invalid zone";
	 Class<?>[] groups() default {};
	 Class<? extends Payload>[] payload() default {};
}
