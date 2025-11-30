package com.lmlasmo.tasklist.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ETagSupport {

	ETagType type() default ETagType.IF_NONE_MATCH;

    enum ETagType {
        IF_MATCH,
        IF_NONE_MATCH
    }
	
}
