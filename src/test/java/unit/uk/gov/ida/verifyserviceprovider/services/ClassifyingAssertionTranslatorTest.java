package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.services.ClassifyingAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

//@RunWith(MockitoJUnitRunner.class)
public class ClassifyingAssertionTranslatorTest {

    private ClassifyingAssertionTranslator classifyingAssertionService;

    @Mock
    private VerifyAssertionTranslator verifyAssertionService;

    @Mock
    private EidasAssertionTranslator eidasAssertionService;


    @Before
    public void setUp() {
        initMocks(this);

        classifyingAssertionService = new ClassifyingAssertionTranslator(
            verifyAssertionService,
            eidasAssertionService
        );
    }

    @Test
    public void shouldUseIdpAssertionServiceIfNoAssertionIsACountryAttributeQuery() {
        Assertion assertion1 = mock(Assertion.class);
        Assertion assertion2 = mock(Assertion.class);
        List<Assertion> assertions = Arrays.asList(assertion1, assertion2);
        String expectedInResponseTo = "somesuch";
        LevelOfAssurance loa = LevelOfAssurance.LEVEL_2;
        String entityId = "someEntityId";
        TranslatedNonMatchingResponseBody expectedResult = mock(TranslatedNonMatchingResponseBody.class);

        when(eidasAssertionService.isCountryAssertion(any())).thenReturn(false);
        when(
            verifyAssertionService.translateSuccessResponse(eq(assertions), eq(expectedInResponseTo), eq(loa), eq(entityId))
        ).thenReturn(expectedResult);

        assertThat(
            classifyingAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId)
        ).isSameAs(expectedResult);
        verify(eidasAssertionService, never()).translateSuccessResponse(any(), any(), any(), any());
    }

    @Test
    public void shouldUseEidasAssertionServiceIfAnyAssertionIsACountryAttributeQuery() {
        Assertion assertion = mock(Assertion.class);
        List<Assertion> assertions = Arrays.asList(assertion);
        String expectedInResponseTo = "somesuch";
        LevelOfAssurance loa = LevelOfAssurance.LEVEL_2;
        String entityId = "someEntityId";
        TranslatedNonMatchingResponseBody expectedResult = mock(TranslatedNonMatchingResponseBody.class);

        when(eidasAssertionService.isCountryAssertion(eq(assertion))).thenReturn(true);
        when(eidasAssertionService.translateSuccessResponse(eq(assertions), eq(expectedInResponseTo), eq(loa), eq(entityId))).thenReturn(expectedResult);


        assertThat(
            classifyingAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId))
            .isSameAs(expectedResult);
        verify(verifyAssertionService, never()).translateSuccessResponse(any(), any(), any(), any());
    }
}
