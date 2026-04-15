package com.rentalplatform.backend.common.response;

import com.rentalplatform.backend.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private String traceId;
    private ErrorCode errorCode;
    private Instant timestamp;

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
                .message(message != null ? message : errorCode.getMessage())
                .errorCode(errorCode)
                .traceId(MDC.get("traceId"))
                .timestamp(Instant.now())
                .build();
    }
}
