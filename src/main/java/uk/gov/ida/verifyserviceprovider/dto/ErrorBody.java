package uk.gov.ida.verifyserviceprovider.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorBody {

    private final String reason;
    private final String message;

    @JsonCreator
    public ErrorBody(
        @JsonProperty("reason") String reason,
        @JsonProperty("message") String message
    ) {
        this.reason = reason;
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorBody errorBody = (ErrorBody) o;

        if (reason != null ? !reason.equals(errorBody.reason) : errorBody.reason != null) return false;
        return message != null ? message.equals(errorBody.message) : errorBody.message == null;
    }

    @Override
    public int hashCode() {
        int result = reason != null ? reason.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ErrorBody{" +
            "reason='" + reason + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
