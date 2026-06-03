package com.rentalplatform.backend.common.security;

import com.rentalplatform.backend.auth.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade{

    @Override
    public UUID getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                                     .getAuthentication();

        CustomUserPrincipal principal =
                (CustomUserPrincipal)
                        authentication.getPrincipal();

        return principal.getId();
    }

    @Override
    public String getCurrentUserEmail() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                                     .getAuthentication();

        CustomUserPrincipal principal =
                (CustomUserPrincipal)
                        authentication.getPrincipal();

        return principal.getEmail();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication =
                SecurityContextHolder.getContext()
                                     .getAuthentication();

        return authentication != null
               && authentication.isAuthenticated();
    }
}
