package defsec.crud.validation;

import defsec.crud.entity.Task;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskStatusValidator implements ConstraintValidator<ValidTaskStatus, String> {

    @Override
    public void initialize(ValidTaskStatus constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String status, ConstraintValidatorContext context) {
        if (status == null) {
            return true; // Let @NotNull handle null validation if needed
        }
        
        // Check if the status matches any of the enum values dynamically
        for (Task.Status enumValue : Task.Status.values()) {
            if (enumValue.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
} 