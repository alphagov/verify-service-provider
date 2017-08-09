package uk.gov.ida.verifyserviceprovider.exceptions;

import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.JerseyViolationException;
import org.apache.http.HttpStatus;
import uk.gov.ida.verifyserviceprovider.dto.ErrorBody;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.stream.Collectors;

public class JerseyViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
    @Override
    public Response toResponse(JerseyViolationException exception) {
        final String errors = exception.getConstraintViolations()
            .stream().map(violation -> ConstraintMessage.getMessage(violation, exception.getInvocable()))
            .collect(Collectors.joining(", "));

        return Response
            .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
            .entity(new ErrorBody(String.valueOf(HttpStatus.SC_UNPROCESSABLE_ENTITY), errors))
            .build();
    }
}
