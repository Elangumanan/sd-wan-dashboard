package com.example.sdwan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sdwanOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SD-WAN Dashboard API")
                        .description("Mock REST API for the SD-WAN monitoring dashboard. " +
                                     "Provides Organization → Site → Device navigation with WAN telemetry.")
                        .version("1.0.0"));
    }
}
