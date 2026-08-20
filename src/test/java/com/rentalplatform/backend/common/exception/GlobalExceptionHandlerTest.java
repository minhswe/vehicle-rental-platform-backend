package com.rentalplatform.backend.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/runtime-exception")
        public void throwRuntimeException() {
            throw new RuntimeException("SQL syntax error in table 'users'");
        }

        @GetMapping("/test/base-exception")
        public void throwBaseException() {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 500 status and generic error message, concealing sensitive internal details")
    void handleException_ShouldReturnGenericErrorMessage_WhenUnexpectedExceptionOccurs() throws Exception {
        String sensitiveErrorMessage = "SQL syntax error in table 'users'";

        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected internal server error occurred. Please try again later."))
                .andExpect(jsonPath("$.message", not(containsString(sensitiveErrorMessage))));
    }

    @Test
    @DisplayName("Should handle BaseException correctly using ErrorCode metadata")
    void handleBaseException_ShouldReturnDefinedErrorCodeAndMessage() throws Exception {
        mockMvc.perform(get("/test/base-exception"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }
}
