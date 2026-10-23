package ke.co.skyworld.internship.util.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

public final class Validation {

    private static final ValidatorFactory FACTORY =
            jakarta.validation.Validation.buildDefaultValidatorFactory();

    private static final Validator VALIDATOR =
            FACTORY.getValidator();

    private Validation() {
        // Prevent instantiation
    }

    public static <T> String validate(T request) {
        Set<ConstraintViolation<T>> violations =
                VALIDATOR.validate(request);

        if (violations.isEmpty()) {
            return null;
        }

        return violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .sorted()
                .collect(Collectors.joining(", "));
    }
}