package uk.gov.ida.verifyserviceprovider.validators;

import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

public class LevelOfAssuranceValidator {

    public void validate(
        LevelOfAssurance levelOfAssurance,
        LevelOfAssurance expectedLevelOfAssurance
    ) {
        if (expectedLevelOfAssurance.isGreaterThan(levelOfAssurance)) {
            throw new SamlResponseValidationException(String.format(
                "Expected Level of Assurance to be at least %s, but was %s",
                expectedLevelOfAssurance,
                levelOfAssurance
            ));
        }
    }
}
