package br.com.brunobs.auth.exception;


import br.com.brunobs.auth.exception.validation.FieldError;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String code;
    private String message;
    private List<FieldError> details;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private final Instant timestamp;

    public ApiError() {
        this.timestamp = Instant.now();
    }

    public ApiError(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ApiError(String code, String message, List<FieldError> details) {
        this(code, message);
        this.details = details;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public List<FieldError> getDetails() { return details; }
    public Instant getTimestamp() { return timestamp; }
}
