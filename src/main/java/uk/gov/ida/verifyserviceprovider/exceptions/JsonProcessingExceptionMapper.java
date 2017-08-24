package uk.gov.ida.verifyserviceprovider.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.jersey.errors.ErrorMessage;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override
    public Response toResponse(JsonProcessingException exception) {
        LOG.warn(String.format("Unable to parse json in request body. %s", exception.getOriginalMessage()));

        return Response
            .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
            .entity(new ErrorMessage(HttpStatus.SC_UNPROCESSABLE_ENTITY, exception.getOriginalMessage()))
            .build();
    }
}
