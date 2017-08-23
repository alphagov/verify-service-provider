package uk.gov.ida.verifyserviceprovider.exceptions;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.JerseyViolationException;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.stream.Collectors;

public class JerseyViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JerseyViolationExceptionMapper.class);

    @Override
    public Response toResponse(JerseyViolationException exception) {
        final String errors = exception.getConstraintViolations()
            .stream().map(violation -> ConstraintMessage.getMessage(violation, exception.getInvocable()))
            .collect(Collectors.joining(", "));

        LOG.warn(String.format("Request body was not valid: %s", errors));

        return Response
            .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
            .entity(new ErrorMessage(HttpStatus.SC_UNPROCESSABLE_ENTITY, errors))
            .build();
    }
}
