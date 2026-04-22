package br.com.brunobs.auth.exception.validation;

public interface Validator<T> {
    ValidationResult validate(T target);
}
