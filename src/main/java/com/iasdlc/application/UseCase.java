package com.iasdlc.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una clase como caso de uso. La capa de infraestructura registra como beans
 * las clases con esta anotación (ver UseCaseConfig), así la capa de aplicación no
 * depende de los estereotipos de Spring.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseCase {
}
