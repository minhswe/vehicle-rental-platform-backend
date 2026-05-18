package com.rentalplatform.backend.auth.security;

import com.rentalplatform.backend.auth.service.JwtService;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //get header authorization
        final String authHeader = request.getHeader("Authorization");
        //If there is no header or not start with "Bearer"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //separate token
            final String jwt = authHeader.substring(7);
            //get email from token
            final String email = jwtService.extractEmail(jwt);
            //only handle if there is no Authentication in SecurityContext
            if (email != null && SecurityContextHolder.getContext()
                                                      .getAuthentication() == null) {
                //find user in database
                User user = userRepository.findByEmail(email)
                                          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                if (user != null && jwtService.isTokenValid(jwt, user)) {

                    //create auth list from role
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()
                                                                             .name()));

                    //principal is userId to use @AuthenticationPrincipal UUID userId
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user.getId(), null, authorities);

                    //assign more request information
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    //save Authentication into SecurityContext
                    SecurityContextHolder.getContext()
                                         .setAuthentication(authToken);
                }
            }


        } catch (Exception ignored) {
            //ignore
        }
        filterChain.doFilter(request, response);

    }
}
