package uk.gov.ida.verifyserviceprovider.compliance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jersey.validation.ConstraintMessage;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;
import org.glassfish.jersey.server.model.Invocable;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

class IdentityDatasetArgumentResolver implements ArgumentType<MatchingDataset> {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public IdentityDatasetArgumentResolver(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public MatchingDataset convert(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
        try {
            return resolveMatchingDataset(value);
        } catch (IOException | MatchingDatasetArgumentValidationError e) {
            throw new ArgumentParserException(e.getMessage(), e, parser);
        }
    }

    private MatchingDataset resolveMatchingDataset(String matchingDatasetJSON) throws IOException, MatchingDatasetArgumentValidationError {
        MatchingDataset matchingDataset = objectMapper.readValue(matchingDatasetJSON, MatchingDataset.class);
        validateMatchingDataset(validator, matchingDataset);
        return matchingDataset;
    }

    private void validateMatchingDataset(Validator validator, MatchingDataset matchingDataset) throws MatchingDatasetArgumentValidationError {
        Set<ConstraintViolation<MatchingDataset>> validations = validator.validate(matchingDataset);
        if(!validations.isEmpty()) {
            final String errors = validations
                    .stream().map(violation -> ConstraintMessage.getMessage(violation, Invocable.create(request -> null)))
                    .collect(Collectors.joining(", "));

            throw new MatchingDatasetArgumentValidationError(String.format("Matching Dataset argument was not valid: %s", errors));
        }
    }

    private class MatchingDatasetArgumentValidationError extends Exception {

        public MatchingDatasetArgumentValidationError(String message) {
            super(message);
        }
    }
}
