package com.lmlasmo.tasklist.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleApiDoc {
    String summary();
    String description() default "";
    StatusResponseApiDoc[] success() default {@StatusResponseApiDoc(status = 200, message = "")};
    StatusResponseApiDoc[] errors() default {};
}
