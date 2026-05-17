package com.rentalplatform.backend.config.filter;

import com.rentalplatform.backend.common.logging.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public TraceIdFilter traceIdFilterBean() {
        return new TraceIdFilter();
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*"); // apply to all api
        registration.setName("traceIdFilter");
        registration.setOrder(1); // high priority

        return registration;
    }
}
