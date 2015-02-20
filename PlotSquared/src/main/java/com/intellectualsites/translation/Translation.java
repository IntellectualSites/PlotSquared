package com.intellectualsites.translation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Translation annotation
 *
 * @author Citymonstret
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Translation {
    String description() default "";
    
    String creationDescription() default "";
}
