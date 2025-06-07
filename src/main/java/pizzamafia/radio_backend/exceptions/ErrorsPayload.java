package pizzamafia.radio_backend.exceptions;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ErrorsPayload {

    private final String message;
    private final Map<String, String> errors = new HashMap<>();

    public ErrorsPayload(String message) {
        this.message = message;
    }

    public void addError(String field, String error) {
        errors.put(field, error);
    }
}
