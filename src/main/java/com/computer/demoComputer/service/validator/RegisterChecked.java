package com.computer.demoComputer.service.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = RegisterValidator.class)
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterChecked {
    String message() default "User register validation failed.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
