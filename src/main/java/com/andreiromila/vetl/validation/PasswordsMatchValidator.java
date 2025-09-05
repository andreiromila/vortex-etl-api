package com.andreiromila.vetl.validation;


import com.andreiromila.vetl.user.web.UserActivationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link PasswordsMatch} annotation.
 * Checks if the 'password' and 'passwordConfirmation' fields of a
 * {@link UserActivationRequest} are equal.
 */
public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, UserActivationRequest> {

    /**
     * The main validation logic.
     *
     * @param request The {@link UserActivationRequest} instance to validate.
     * @param context Context in which the constraint is evaluated.
     * @return {@code true} if passwords are null or match, {@code false} otherwise.
     */
    @Override
    public boolean isValid(UserActivationRequest request, ConstraintValidatorContext context) {
        // Si cualquiera de los dos campos es nulo, dejamos que @NotNull se encargue.
        // Nuestra lógica solo debe comparar si ambos están presentes.
        if (request.password() == null || request.passwordConfirmation() == null) {
            return true;
        }

        // Devuelve true si las contraseñas son iguales.
        return request.password().equals(request.passwordConfirmation());
    }
}