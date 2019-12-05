package uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.StatusCode;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.domain.SamlStatusCode;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.dto.MatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.builders.StatusCodeBuilder.aStatusCode;

public class MatchingResponderCodeTranslatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private MatchingResponderCodeTranslator msaAssertionService = new MatchingResponderCodeTranslator();

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithNoSubStatusCode() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Missing status code for non-Success response");

        StatusCode statusCode = aStatusCode().withValue(StatusCode.RESPONDER).build();
        msaAssertionService.translateResponderCode(statusCode);
    }

    @Test
    public void shouldReturnScenarioCancelledWhenNoAuthnContextStatus() {
        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AUTHN_CONTEXT).build())
            .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.CANCELLATION);
    }

    @Test
    public void shouldReturnScenarioNoMatchWhenNoMatchStatus() {
        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(SamlStatusCode.NO_MATCH).build())
            .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.NO_MATCH);
    }

    @Test
    public void shouldReturnScenarioAuthenticationFailedWhenAuthnFailedStatus() {
        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.AUTHN_FAILED).build())
            .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.AUTHENTICATION_FAILED);
    }

    @Test
    public void shouldReturnScenarioRequestErrorWhenRequesterStatus() {
        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.REQUESTER).build())
            .build();
        TranslatedResponseBody response = msaAssertionService.translateResponderCode(statusCode);
        assertThat(response.getScenario()).isEqualTo(MatchingScenario.REQUEST_ERROR);
    }

    @Test
    public void shouldThrowExceptionWhenNonSuccessResponseCalledWithUnrecognisedStatus() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Unknown SAML sub-status: urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP");

        StatusCode statusCode = aStatusCode()
            .withValue(StatusCode.RESPONDER)
            .withSubStatusCode(aStatusCode().withValue(StatusCode.NO_AVAILABLE_IDP).build())
            .build();
        msaAssertionService.translateResponderCode(statusCode);
    }
}
