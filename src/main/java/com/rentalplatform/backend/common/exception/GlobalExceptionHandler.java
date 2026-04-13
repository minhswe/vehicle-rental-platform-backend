package com.rentalplatform.backend.common.exception;

import com.rentalplatform.backend.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // VALIDATION ERROR (DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, ErrorCode.valueOf(ErrorCode.VALIDATION_ERROR.name())));
    }

    // CUSTOM BUSINESS EXCEPTION
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex) {

        HttpStatus status = switch (ex.getErrorCode()) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage(), ErrorCode.valueOf(ex.getErrorCode().name())));
    }

    // RUNTIME (fallback business error)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), ErrorCode.valueOf(ErrorCode.INTERNAL_ERROR.name())));
    }

    // UNKNOWN SYSTEM ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {

        ex.printStackTrace(); //or using logger

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", ErrorCode.valueOf(ErrorCode.INTERNAL_ERROR.name())));
    }


}