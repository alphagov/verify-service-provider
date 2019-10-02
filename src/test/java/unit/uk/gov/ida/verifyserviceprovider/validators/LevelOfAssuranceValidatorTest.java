package unit.uk.gov.ida.verifyserviceprovider.validators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;

public class LevelOfAssuranceValidatorTest {

    private LevelOfAssuranceValidator validator = new LevelOfAssuranceValidator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldValidateThatTheLevelOfAssuranceExceedsTheOneExpected() {
        LevelOfAssurance levelOfAssurance = LevelOfAssurance.LEVEL_2;
        LevelOfAssurance expectedLevelOfAssurance = LevelOfAssurance.LEVEL_1;

        validator.validate(levelOfAssurance, expectedLevelOfAssurance);
    }

    @Test
    public void shouldThrowExceptionWhenLevelOfAssuranceIsLessThenExpected() {
        LevelOfAssurance levelOfAssurance = LevelOfAssurance.LEVEL_1;
        LevelOfAssurance expectedLevelOfAssurance = LevelOfAssurance.LEVEL_2;

        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage(String.format(
            "Expected Level of Assurance to be at least %s, but was %s",
            expectedLevelOfAssurance,
            levelOfAssurance
        ));

        validator.validate(levelOfAssurance, expectedLevelOfAssurance);
    }
}