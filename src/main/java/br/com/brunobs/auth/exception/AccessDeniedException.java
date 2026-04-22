package br.com.brunobs.auth.exception;


import br.com.brunobs.auth.exception.validation.ValidationResult;

public class AccessDeniedException extends RuntimeException {

    private final ValidationResult validationResult;

    public AccessDeniedException(ValidationResult validationResult) {
        super("Validation failed with " + (validationResult != null ? validationResult.getErrors().size() : 0) + " error(s).");
        this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}
