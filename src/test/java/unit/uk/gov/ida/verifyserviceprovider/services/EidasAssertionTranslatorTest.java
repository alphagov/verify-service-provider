package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.domain.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.saml.core.extensions.EidasAuthnContext.EIDAS_LOA_HIGH;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class EidasAssertionTranslatorTest extends BaseEidasAssertionTranslatorTestBase {

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);
        assertionService = new EidasAssertionTranslator(
                getEidasAssertionTranslatorValidatorContainer(),
                eidasMatchingDatasetUnmarshaller,
                mdsMapper,
                metadataResolverRepository,
                signatureValidatorFactory,
                singletonList(HUB_CONNECTOR_ENTITY_ID),
                userIdHashFactory);
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        doNothing().when(levelOfAssuranceValidator).validate(any(), any());
        when(metadataResolverRepository.getResolverEntityIds()).thenReturn(singletonList(STUB_COUNTRY_ONE));
        ExplicitKeySignatureTrustEngine mock = mock(ExplicitKeySignatureTrustEngine.class);
        when(metadataResolverRepository.getSignatureTrustEngine(same(STUB_COUNTRY_ONE))).thenReturn(Optional.of(mock));
        when(signatureValidatorFactory.getSignatureValidator(same(mock))).thenReturn(samlAssertionsSignatureValidator);
        when(samlAssertionsSignatureValidator.validate(any(), any())).thenReturn(null);
        when(mdsMapper.mapToNonMatchingAttributes(any())).thenReturn(mock(NonMatchingAttributes.class));
    }

    @Override
    @Test
    public void shouldCallValidatorsCorrectly() {
        List<Assertion> assertions = singletonList(
                anAssertionWithAuthnStatement(EIDAS_LOA_HIGH, "requestId").buildUnencrypted());

        assertionService.translateSuccessResponse(assertions, "requestId", LEVEL_2, null);
        verify(instantValidator, times(1)).validate(any(), any());
        verify(subjectValidator, times(1)).validate(any(), any());
        verify(conditionsValidator, times(1)).validate(any(), any());
        verify(levelOfAssuranceValidator, times(1)).validate(any(), any());
        verify(samlAssertionsSignatureValidator, times(1)).validate(any(), any());
    }
}
