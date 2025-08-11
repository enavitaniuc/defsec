package defsec.crud.exception;

public class ConflictException extends RuntimeException {
    private final String field;
    private final String conflictValue;

    public ConflictException(String message, String field, String conflictValue) {
        super(message);
        this.field = field;
        this.conflictValue = conflictValue;
    }

    public String getField() {
        return field;
    }

    public String getConflictValue() {
        return conflictValue;
    }
} 