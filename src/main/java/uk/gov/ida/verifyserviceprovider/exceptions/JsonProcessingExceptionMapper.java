package uk.gov.ida.verifyserviceprovider.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.jersey.errors.ErrorMessage;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    @Override
    public Response toResponse(JsonProcessingException exception) {
        return Response
            .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
            .entity(new ErrorMessage(HttpStatus.SC_UNPROCESSABLE_ENTITY, exception.getOriginalMessage()))
            .build();
    }
}
