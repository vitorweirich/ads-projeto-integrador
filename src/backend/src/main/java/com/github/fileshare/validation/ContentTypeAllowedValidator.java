package com.github.fileshare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.github.fileshare.utils.AllowedContentTypes;

public class ContentTypeAllowedValidator implements ConstraintValidator<ContentTypeAllowed, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        boolean valid = AllowedContentTypes.isAllowed(value);
        if (!valid) {
            context.disableDefaultConstraintViolation();
            String allowed = String.join(", ", AllowedContentTypes.getAllowedTypes());
            context.buildConstraintViolationWithTemplate(
                "O tipo de conteúdo deve ser um dos permitidos: " + allowed
            ).addConstraintViolation();
        }
        return valid;
    }
}
