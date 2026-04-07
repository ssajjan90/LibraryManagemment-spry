package com.example.library.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class PublishedYearValidator implements ConstraintValidator<ValidPublishedYear, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        int currentYear = Year.now().getValue();
        return value >= 1450 && value <= currentYear;
    }
}
