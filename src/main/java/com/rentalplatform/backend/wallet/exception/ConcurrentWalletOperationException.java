package com.rentalplatform.backend.wallet.exception;

import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;

public class ConcurrentWalletOperationException extends AppException {

    public ConcurrentWalletOperationException() {
        super(ErrorCode.CONCURRENT_WALLET_CONFLICT);
    }

    public ConcurrentWalletOperationException(String message) {
        super(ErrorCode.CONCURRENT_WALLET_CONFLICT);
    }

    public ConcurrentWalletOperationException(String message, Throwable cause) {
        super(ErrorCode.CONCURRENT_WALLET_CONFLICT);
        initCause(cause);
    }
}
