package br.com.brunobs.auth.web.headers;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class RequestHeadersValidator {

    private static final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    public static void validate(RequestHeaders headers) {

        var violations = validator.validate(headers);

        if (!violations.isEmpty()) {

            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .reduce((a,b) -> a + ", " + b)
                    .orElse("Invalid headers");

            throw new IllegalArgumentException(errors);
        }
    }

}