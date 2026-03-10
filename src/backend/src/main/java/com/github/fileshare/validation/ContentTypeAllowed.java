package com.github.fileshare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContentTypeAllowedValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentTypeAllowed {
    String message() default "Tipo de conteúdo não permitido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
