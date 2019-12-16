package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.services.ClassifyingAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasUnsignedAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClassifyingAssertionTranslatorTest {

    private final String expectedInResponseTo = "inResponseTo";
    private final LevelOfAssurance loa = LevelOfAssurance.LEVEL_2;
    private final String entityId = "entityId";

    private ClassifyingAssertionTranslator classifyingAssertionService;

    @Mock
    private VerifyAssertionTranslator verifyAssertionService;

    @Mock
    private EidasAssertionTranslator eidasAssertionService;

    @Mock
    private EidasUnsignedAssertionTranslator eidasUnsignedAssertionTranslator;

    @Before
    public void setUp() {
        initMocks(this);

        classifyingAssertionService = new ClassifyingAssertionTranslator(
                verifyAssertionService,
                eidasAssertionService,
                eidasUnsignedAssertionTranslator
        );
    }

    @Test
    public void shouldUseIdpAssertionServiceIfNoAssertionIsACountryAttributeQuery() {
        Assertion assertion1 = mock(Assertion.class);
        Assertion assertion2 = mock(Assertion.class);
        List<Assertion> assertions = Arrays.asList(assertion1, assertion2);
        TranslatedNonMatchingResponseBody expectedResult = mock(TranslatedNonMatchingResponseBody.class);

        when(eidasAssertionService.isCountryAssertion(any())).thenReturn(false);
        when(verifyAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId)).thenReturn(expectedResult);


        TranslatedNonMatchingResponseBody actualResult = (TranslatedNonMatchingResponseBody) classifyingAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId);


        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldUseEidasAssertionServiceIfAnyAssertionIsACountryAttributeQuery() {
        Assertion assertion1 = mock(Assertion.class);
        Assertion assertion2 = mock(Assertion.class);
        List<Assertion> assertions = Arrays.asList(assertion1, assertion2);
        TranslatedNonMatchingResponseBody expectedResult = mock(TranslatedNonMatchingResponseBody.class);

        when(assertion1.getSignature()).thenReturn(mock(SignatureImpl.class));
        when(eidasAssertionService.isCountryAssertion(assertion1)).thenReturn(false);
        when(eidasAssertionService.isCountryAssertion(assertion2)).thenReturn(true);
        when(eidasAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId)).thenReturn(expectedResult);


        TranslatedNonMatchingResponseBody actualResult = (TranslatedNonMatchingResponseBody) classifyingAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId);


        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldUseEidasUnsignedAssertionTranslatorIfAssertionsAreUnsigned() {
        Assertion assertion = mock(Assertion.class);
        List<Assertion> assertions = Arrays.asList(assertion);
        TranslatedNonMatchingResponseBody expectedResult = mock(TranslatedNonMatchingResponseBody.class);

        when(assertion.getSignature()).thenReturn(null);
        when(eidasAssertionService.isCountryAssertion(assertion)).thenReturn(true);
        when(eidasUnsignedAssertionTranslator.translateSuccessResponse(
                assertions,
                expectedInResponseTo,
                loa,
                entityId)
        ).thenReturn(expectedResult);

        TranslatedNonMatchingResponseBody actualResult =
                (TranslatedNonMatchingResponseBody) classifyingAssertionService
                        .translateSuccessResponse(
                                assertions,
                                expectedInResponseTo,
                                loa,
                                entityId
                        );

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
