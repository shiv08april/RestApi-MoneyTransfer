package com.shiv.rest.mt.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class PrecisionValidator implements ConstraintValidator<Precision, BigDecimal> {

    private int precision;

    @Override
    public void initialize(Precision constraintAnnotation) {
        precision = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return value == null || value.scale() <= precision;
    }
}
