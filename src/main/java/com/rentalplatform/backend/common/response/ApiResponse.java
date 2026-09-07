package com.rentalplatform.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rentalplatform.backend.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private Integer status;
    private T data;
    private String message;
    private String traceId;
    private ErrorCode errorCode;
    private Instant timestamp;
    private Map<String, String> errors;

    //SUCCESS
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .traceId(MDC.get("traceId"))
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    //ERROR
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(errorCode.getStatus())
                .message(message != null ? message : errorCode.getMessage())
                .errorCode(errorCode)
                .traceId(MDC.get("traceId"))
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> validationError(Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(ErrorCode.VALIDATION_ERROR.getStatus())
                .message("Validation failed")
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .errors(errors)
                .traceId(MDC.get("traceId"))
                .timestamp(Instant.now())
                .build();
    }
}
