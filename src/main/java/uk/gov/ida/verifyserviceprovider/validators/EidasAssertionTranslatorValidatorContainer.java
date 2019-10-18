package uk.gov.ida.verifyserviceprovider.validators;

public class EidasAssertionTranslatorValidatorContainer {
    private final SubjectValidator subjectValidator;
    private final InstantValidator instantValidator;
    private final ConditionsValidator conditionsValidator;
    private final LevelOfAssuranceValidator levelOfAssuranceValidator;

    public EidasAssertionTranslatorValidatorContainer(
            SubjectValidator subjectValidator,
            InstantValidator instantValidator,
            ConditionsValidator conditionsValidator,
            LevelOfAssuranceValidator levelOfAssuranceValidator
    ) {
        this.subjectValidator = subjectValidator;
        this.instantValidator = instantValidator;
        this.conditionsValidator = conditionsValidator;
        this.levelOfAssuranceValidator = levelOfAssuranceValidator;
    };

    public SubjectValidator getSubjectValidator() { return subjectValidator; }
    public InstantValidator getInstantValidator() { return instantValidator; }
    public ConditionsValidator getConditionsValidator() { return conditionsValidator; }
    public LevelOfAssuranceValidator getLevelOfAssuranceValidator() { return levelOfAssuranceValidator; }
}
