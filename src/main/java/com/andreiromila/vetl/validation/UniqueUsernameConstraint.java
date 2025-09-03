package com.andreiromila.vetl.validation;

import com.andreiromila.vetl.user.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUsernameConstraint implements ConstraintValidator<UniqueUsername, String> {

    private final UserRepository userRepository;

    public UniqueUsernameConstraint(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return userRepository.findByUsername(value).isEmpty();
    }
}
