package com.galpi.galpibackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GalpiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GalpiBackendApplication.class, args);
    }

}