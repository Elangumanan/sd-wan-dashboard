package com.example.sdwan.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Jackson to serialise using private final fields directly.
 * This supports immutable classes that expose record-style accessors (id(), name())
 * rather than JavaBean-style getters (getId(), getName()).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer fieldVisibilityCustomizer() {
        return builder -> builder
                .visibility(PropertyAccessor.FIELD,      JsonAutoDetect.Visibility.ANY)
                .visibility(PropertyAccessor.GETTER,     JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.IS_GETTER,  JsonAutoDetect.Visibility.NONE);
    }
}
