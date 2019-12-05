package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier.AssertionType;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class AssertionClassifierTests {
    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldClassifyAnAssertionBasedOnWhetherItContainsAuthnStatements() {
        Assertion mdsAssertion = aMatchingDatasetAssertion("requestId").buildUnencrypted();
        Assertion authnStatementAssertion = anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "requestId").buildUnencrypted();

        AssertionClassifier assertionClassifier = new AssertionClassifier();

        assertThat(assertionClassifier.classifyAssertion(mdsAssertion)).isEqualTo(AssertionType.MDS_ASSERTION);
        assertThat(assertionClassifier.classifyAssertion(authnStatementAssertion)).isEqualTo(AssertionType.AUTHN_ASSERTION);
    }

    public static AssertionBuilder aMatchingDatasetAssertion(String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().build());
    }

    public static AssertionBuilder anAuthnStatementAssertion( String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                        anAuthnStatement()
                                .withAuthnContext(
                                        anAuthnContext()
                                                .withAuthnContextClassRef(
                                                        anAuthnContextClassRef()
                                                                .withAuthnContextClasRefValue(authnContext)
                                                                .build())
                                                .build())
                                .build())
                .withSubject(
                        aSubject()
                                .withSubjectConfirmation(
                                        aSubjectConfirmation()
                                                .withSubjectConfirmationData(
                                                        aSubjectConfirmationData()
                                                                .withInResponseTo(inResponseTo)
                                                                .build()
                                                ).build()
                                ).build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build());
    }

}
