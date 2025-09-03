package com.andreiromila.vetl.validation;

import com.andreiromila.vetl.user.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailConstraint implements ConstraintValidator<UniqueEmail, String> {

    private final UserRepository userRepository;

    public UniqueEmailConstraint(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Indicates if the current user is already registered in the database
        return userRepository.findByEmail(email).isEmpty();
    }

}