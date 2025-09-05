package com.andreiromila.vetl.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to verify that two password fields match.
 * To be used at the class level on a DTO or record.
 * <p>
 * The class must have methods named 'password()' and 'passwordConfirmation()'.
 */
@Constraint(validatedBy = PasswordsMatchValidator.class) // Apunta a nuestra lógica de validación
@Target({ ElementType.TYPE }) // Se puede aplicar solo a nivel de clase/record
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {

    // Mensaje de error por defecto si la validación falla
    String message() default "The passwords do not match.";

    // Grupos de validación (generalmente no se necesita para validaciones simples)
    Class<?>[] groups() default {};

    // Cargas útiles de metadatos (raramente se usa)
    Class<? extends Payload>[] payload() default {};
}
