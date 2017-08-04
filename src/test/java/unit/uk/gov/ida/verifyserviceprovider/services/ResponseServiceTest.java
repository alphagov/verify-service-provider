package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class ResponseServiceTest {

    private ResponseService responseService;

    private StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer;
    private AssertionDecrypter assertionDecrypter;
    private Response response;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        stringToOpenSamlObjectTransformer = mock(StringToOpenSamlObjectTransformer.class);
        assertionDecrypter = mock(AssertionDecrypter.class);
        response = mock(Response.class);

        when(stringToOpenSamlObjectTransformer.apply(ArgumentMatchers.any())).thenReturn(response);

        responseService = new ResponseService(
            stringToOpenSamlObjectTransformer,
            assertionDecrypter
        );
    }

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldWithExpectedPid() throws Exception {
        Assertion assertion = anAssertionWithPid("expected-pid");
        when(assertionDecrypter.decryptAssertions(ArgumentMatchers.any())).thenReturn(ImmutableList.of(assertion));

        TranslatedResponseBody translatedResponseBody = responseService.convertTranslatedResponseBody("");
        assertThat(translatedResponseBody.getPid()).isEqualTo("expected-pid");
    }

    @Test
    public void shouldWithExpectedLevelOfAssurance() throws Exception {
        Assertion assertion = anAssertionWithLevelOfAssurance(LEVEL_2.name());
        when(assertionDecrypter.decryptAssertions(ArgumentMatchers.any())).thenReturn(ImmutableList.of(assertion));

        TranslatedResponseBody translatedResponseBody = responseService.convertTranslatedResponseBody("");
        assertThat(translatedResponseBody.getLevelOfAssurance()).isEqualTo(LEVEL_2);
    }

    @Test
    public void shouldWithExpectedAttributesEmpty() throws Exception {
        Assertion assertion = anAssertion().buildUnencrypted();
        when(assertionDecrypter.decryptAssertions(ArgumentMatchers.any())).thenReturn(ImmutableList.of(assertion));

        TranslatedResponseBody translatedResponseBody = responseService.convertTranslatedResponseBody("");
        assertThat(translatedResponseBody.getAttributes()).isEmpty();
    }

    @Test(expected = SamlResponseValidationException.class)
    public void shouldThrowExceptionWithUnknownLevelOfAssurance() throws Exception {
        Assertion assertion = anAssertionWithLevelOfAssurance("unknown");
        when(assertionDecrypter.decryptAssertions(ArgumentMatchers.any())).thenReturn(ImmutableList.of(assertion));

        responseService.convertTranslatedResponseBody("");
    }

    @Test
    public void shouldThrowExceptionWithNoAssertions() throws Exception {
        when(assertionDecrypter.decryptAssertions(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Only one assertion is expected.");

        responseService.convertTranslatedResponseBody("");
    }

    @Test
    public void shouldThrowExceptionWhenSubjectIsMissing() throws Exception {
        Assertion assertion = anAssertion().withSubject(null).buildUnencrypted();
        when(assertionDecrypter.decryptAssertions(ArgumentMatchers.any())).thenReturn(ImmutableList.of(assertion));
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject is missing from the assertion.");

        responseService.convertTranslatedResponseBody("");
    }

    @Test
    public void shouldThrowExceptionWhenNameIdIsMissing() throws Exception {
        Assertion assertion = anAssertion().withSubject(aSubject().withNameId(null).build()).buildUnencrypted();
        when(assertionDecrypter.decryptAssertions(ArgumentMatchers.any())).thenReturn(ImmutableList.of(assertion));
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("NameID is missing from the subject of the assertion.");

        responseService.convertTranslatedResponseBody("");
    }

    private Assertion anAssertionWithLevelOfAssurance(String levelOfAssurance) {
        return anAssertion()
                .addAuthnStatement(anAuthnStatement()
                    .withAuthnContext(anAuthnContext()
                        .withAuthnContextClassRef(anAuthnContextClassRef()
                            .withAuthnContextClasRefValue(levelOfAssurance).build())
                        .build())
                    .build())
                .buildUnencrypted();
    }

    private Assertion anAssertionWithPid(String expectedPid) {
        return anAssertion().withSubject(
                aSubject().withPersistentId(expectedPid).build()
        ).buildUnencrypted();
    }

}