package com.example.library.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PublishedYearValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPublishedYear {
    String message() default "publishedYear must be between 1450 and current year";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
