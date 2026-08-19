package com.rentalplatform.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class VehicleRentalPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleRentalPlatformApplication.class, args);
    }

}
