package com.rentalplatform.backend.common.exception;

public class AppException extends BaseException {
    public AppException(ErrorCode errorCode){
        super(errorCode);
    }
}
