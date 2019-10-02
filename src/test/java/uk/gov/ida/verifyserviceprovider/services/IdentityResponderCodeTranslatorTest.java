package uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.builders.StatusCodeBuilder.aStatusCode;

public class IdentityResponderCodeTranslatorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private IdentityResponderCodeTranslator responderResponseTranslator = new IdentityResponderCodeTranslator();

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithNoSubStatusCode() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Missing status code for non-Success response");

        StatusCode statusCode = aStatusCode().withValue(StatusCode.RESPONDER).build();
        responderResponseTranslator.translateResponderCode(statusCode);
    }

    @Test
    public void shouldReturnScenarioNoAuthenticationWhenNoAuthnContextStatus() {
        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AUTHN_CONTEXT).build())
            .build();
        TranslatedResponseBody response = responderResponseTranslator.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(NonMatchingScenario.NO_AUTHENTICATION);
    }

    @Test
    public void shouldReturnScenarioAuthenticationFailedWhenAuthnFailedStatus() {
        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.AUTHN_FAILED).build())
            .build();
        TranslatedResponseBody response = responderResponseTranslator.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(NonMatchingScenario.AUTHENTICATION_FAILED);
    }

    @Test
    public void shouldReturnScenarioRequestErrorWhenRequesterStatus() {
        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.REQUESTER).build())
            .build();
        TranslatedResponseBody response = responderResponseTranslator.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(NonMatchingScenario.REQUEST_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithUnrecognisedStatus() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Unknown SAML sub-status: urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP");

        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AVAILABLE_IDP).build())
            .build();
        responderResponseTranslator.translateResponderCode(statusCode);
    }

}