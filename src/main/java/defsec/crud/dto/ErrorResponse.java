package defsec.crud.dto;

public class ErrorResponse {
    private String error;
    private String message;
    private String field;

    public ErrorResponse(String error, String message, String field) {
        this.error = error;
        this.message = message;
        this.field = field;
    }

    // Getters and setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
} 