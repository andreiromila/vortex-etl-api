package com.andreiromila.vetl.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD})
@Constraint(validatedBy = UniqueEmailConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {

    // Define the default message
    String message() default "The email is already taken.";

    // Default groups
    Class<?>[] groups() default {};

    // Default payload
    Class<? extends Payload>[] payload() default {};

}
