package com.iasdlc.infrastructure.config;

import com.iasdlc.application.UseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.time.Clock;
import java.time.Duration;

/**
 * Registra como beans las clases de la capa de aplicación marcadas con {@link UseCase},
 * y el reloj que usan los casos de uso.
 */
@Configuration
@ComponentScan(
        basePackages = "com.iasdlc.application",
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
public class UseCaseConfig {

    /**
     * Reloj en UTC con precisión de microsegundos, la misma que guarda la base de datos,
     * para que una fecha recién creada sea igual a la que luego se lee.
     */
    @Bean
    public Clock clock() {
        return Clock.tick(Clock.systemUTC(), Duration.ofNanos(1_000));
    }
}
