package com.rentalplatform.backend.common.exception;

import com.rentalplatform.backend.common.constant.ApiPaths;
import com.rentalplatform.backend.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===================== VALIDATION ERROR =====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String requestPath = request.getRequestURI();

        // allow swagger to bypass
        if (isSwaggerPath(requestPath)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, "Validation error"));
        }

        String message = ex.getBindingResult()
                           .getFieldErrors()
                           .stream()
                           .map(err -> err.getField() + ": " + err.getDefaultMessage())
                           .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    // ===================== BUSINESS EXCEPTION =====================
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseException(
            BaseException ex,
            HttpServletRequest request) {

        String requestPath = request.getRequestURI();

        // usually NOT needed, but keeping consistency
        if (isSwaggerPath(requestPath)) {
            throw ex;
        }

        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode, errorCode.getMessage()));
    }

    // ===================== UNKNOWN ERROR =====================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(
            Exception ex,
            HttpServletRequest request) {

        String requestPath = request.getRequestURI();

        if (isSwaggerPath(requestPath)) {
            throw new RuntimeException(ex);
        }

        log.error("Unexpected error: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, ex.getMessage()));
    }

    // ===================== HELPER =====================
    private boolean isSwaggerPath(String path) {
        return path.startsWith(ApiPaths.API_DOCS)
               || path.startsWith(ApiPaths.SWAGGER_UI)
               || path.startsWith(ApiPaths.WEBJARS);
    }
}