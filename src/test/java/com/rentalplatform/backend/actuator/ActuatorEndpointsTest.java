package com.rentalplatform.backend.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ActuatorEndpointsTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /actuator/health returns 200 OK with status UP without authentication")
    void getHealth_Unauthenticated_ReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health/liveness returns 200 OK with status UP")
    void getLivenessProbe_Unauthenticated_ReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/health/readiness returns 200 OK with status UP")
    void getReadinessProbe_Unauthenticated_ReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("GET /actuator/prometheus returns 401 or 403 when unauthenticated")
    void getPrometheus_Unauthenticated_ReturnsForbiddenOrUnauthorized() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /actuator/prometheus returns 403 Forbidden for CUSTOMER role")
    @WithMockUser(roles = "CUSTOMER")
    void getPrometheus_CustomerRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /actuator/prometheus returns 200 OK and text formatted metrics for MONITORING role")
    @WithMockUser(roles = "MONITORING")
    void getPrometheus_MonitoringRole_ReturnsPrometheusMetrics() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/plain")));
    }

    @Test
    @DisplayName("GET /actuator/metrics returns 200 OK for ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void getMetrics_AdminRole_ReturnsOk() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.names").exists());
    }

    @Test
    @DisplayName("GET /actuator/env returns 404 Not Found as it is not exposed")
    @WithMockUser(roles = "ADMIN")
    void getEnv_UnexposedEndpoint_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /actuator/beans returns 404 Not Found as it is not exposed")
    @WithMockUser(roles = "ADMIN")
    void getBeans_UnexposedEndpoint_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /actuator/heapdump returns 404 Not Found as it is not exposed")
    @WithMockUser(roles = "ADMIN")
    void getHeapdump_UnexposedEndpoint_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/actuator/heapdump"))
                .andExpect(status().isNotFound());
    }
}
