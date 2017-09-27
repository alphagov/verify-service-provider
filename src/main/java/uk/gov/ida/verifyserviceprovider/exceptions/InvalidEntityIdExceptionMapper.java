package uk.gov.ida.verifyserviceprovider.exceptions;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class InvalidEntityIdExceptionMapper implements ExceptionMapper<InvalidEntityIdException> {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(InvalidEntityIdExceptionMapper.class);

    @Override
    public Response toResponse(InvalidEntityIdException exception) {
        LOG.warn(String.format("Request invalid for this service provider. %s", exception.getMessage()));

        return Response
            .status(HttpStatus.SC_BAD_REQUEST)
            .entity(new ErrorMessage(HttpStatus.SC_BAD_REQUEST, exception.getMessage()))
            .build();
    }
}
