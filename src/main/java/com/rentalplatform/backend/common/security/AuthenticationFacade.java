package com.rentalplatform.backend.common.security;

import java.util.UUID;

public interface AuthenticationFacade {
    UUID getCurrentUserId();

    String getCurrentUserEmail();

    boolean isAuthenticated();
}
