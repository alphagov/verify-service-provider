package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.services.ClassifyingAssertionService;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionService;
import uk.gov.ida.verifyserviceprovider.services.IdpAssertionService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClassifyingAssertionServiceTest {

    private ClassifyingAssertionService classifyingAssertionService;

    @Mock
    private IdpAssertionService idpAssertionService;

    @Mock
    private EidasAssertionService eidasAssertionService;


    @Before
    public void setUp() {
        initMocks(this);

        classifyingAssertionService = new ClassifyingAssertionService(
                idpAssertionService,
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
        when(idpAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId)).thenReturn(expectedResult);


        TranslatedNonMatchingResponseBody actualResult = (TranslatedNonMatchingResponseBody) classifyingAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId);


        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldUseEidasAssertionServiceIfAnyAssertionIsACountryAttributeQuery() {
        Assertion assertion1 = mock(Assertion.class);
        Assertion assertion2 = mock(Assertion.class);
        List<Assertion> assertions = Arrays.asList(assertion1, assertion2);
        String expectedInResponseTo = "somesuch";
        LevelOfAssurance loa = LevelOfAssurance.LEVEL_2;
        String entityId = "someEntityId";
        TranslatedNonMatchingResponseBody expectedResult = mock(TranslatedNonMatchingResponseBody.class);

        when(eidasAssertionService.isCountryAssertion(assertion1)).thenReturn(false);
        when(eidasAssertionService.isCountryAssertion(assertion2)).thenReturn(true);
        when(eidasAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId)).thenReturn(expectedResult);


        TranslatedNonMatchingResponseBody actualResult = (TranslatedNonMatchingResponseBody) classifyingAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId);


        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
