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
    private String errorCode;
    private String timestamp;

    //SUCCESS
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .traceId(MDC.get("traceId"))
                .timestamp(Instant.now().toString())
                .build();
    }

    //ERROR
    public static <T> ApiResponse<T> error(String message, ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(String.valueOf(errorCode))
                .traceId(MDC.get("traceId"))
                .timestamp(Instant.now().toString())
                .build();
    }
}
