package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.services.ClassifyingAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Before
    public void setUp() {
        initMocks(this);

        classifyingAssertionService = new ClassifyingAssertionTranslator(verifyAssertionService);
    }

    @Test
    public void shouldUseIdpAssertionService() {
        Assertion assertion1 = mock(Assertion.class);
        Assertion assertion2 = mock(Assertion.class);
        List<Assertion> assertions = Arrays.asList(assertion1, assertion2);
        TranslatedNonMatchingResponseBody expectedResult = mock(TranslatedNonMatchingResponseBody.class);

        when(verifyAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId)).thenReturn(expectedResult);


        TranslatedNonMatchingResponseBody actualResult = (TranslatedNonMatchingResponseBody) classifyingAssertionService.translateSuccessResponse(assertions, expectedInResponseTo, loa, entityId);

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
