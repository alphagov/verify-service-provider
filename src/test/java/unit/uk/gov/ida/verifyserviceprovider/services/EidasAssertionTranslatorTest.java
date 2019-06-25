package unit.uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.transformers.EidasMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TestTranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.factories.saml.SignatureValidatorFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.saml.core.extensions.EidasAuthnContext.EIDAS_LOA_HIGH;
import static uk.gov.ida.saml.core.extensions.EidasAuthnContext.EIDAS_LOA_SUBSTANTIAL;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class EidasAssertionTranslatorTest {

    private EidasAssertionTranslator eidasAssertionService;
    @Mock
    private SubjectValidator subjectValidator;
    @Mock
    private EidasMatchingDatasetUnmarshaller eidasMatchingDatasetUnmarshaller;
    @Mock
    private MatchingDatasetToNonMatchingAttributesMapper mdsMapper;
    @Mock
    private InstantValidator instantValidator;
    @Mock
    private ConditionsValidator conditionsValidator;
    @Mock
    private LevelOfAssuranceValidator levelOfAssuranceValidator;
    @Mock
    private EidasMetadataResolverRepository metadataResolverRepository;
    @Mock
    private SignatureValidatorFactory signatureValidatorFactory;
    @Mock
    private SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    @Mock
    private UserIdHashFactory userIdHashFactory;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);
        eidasAssertionService = new EidasAssertionTranslator(
            subjectValidator,
            eidasMatchingDatasetUnmarshaller,
            mdsMapper,
            instantValidator,
            conditionsValidator,
            levelOfAssuranceValidator,
            metadataResolverRepository,
            signatureValidatorFactory,
            HUB_CONNECTOR_ENTITY_ID,
            userIdHashFactory);
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        doNothing().when(levelOfAssuranceValidator).validate(any(), any());
        when(metadataResolverRepository.getResolverEntityIds()).thenReturn(asList(STUB_COUNTRY_ONE));
        ExplicitKeySignatureTrustEngine mock = mock(ExplicitKeySignatureTrustEngine.class);
        when(metadataResolverRepository.getSignatureTrustEngine(same(STUB_COUNTRY_ONE))).thenReturn(Optional.of(mock));
        when(signatureValidatorFactory.getSignatureValidator(same(mock))).thenReturn(samlAssertionsSignatureValidator);
        when(samlAssertionsSignatureValidator.validate(any(), any())).thenReturn(null);
        when(mdsMapper.mapToNonMatchingAttributes(any())).thenReturn(mock(NonMatchingAttributes.class));
    }

    @Test
    public void shouldCallValidatorsCorrectly() {
        List<Assertion> assertions = asList(
            anAssertionWithAuthnStatement(EIDAS_LOA_HIGH, "requestId").buildUnencrypted());

        eidasAssertionService.translateSuccessResponse(assertions, "requestId", LEVEL_2, null);
        verify(instantValidator, times(1)).validate(any(), any());
        verify(subjectValidator, times(1)).validate(any(), any());
        verify(conditionsValidator, times(1)).validate(any(), any());
        verify(levelOfAssuranceValidator, times(1)).validate(any(), any());
    }

    @Test
    public void shouldTranslateEidasAssertion() {
        Assertion eidasAssertion = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();
        eidasAssertionService.translateSuccessResponse(singletonList(eidasAssertion), "requestId", LEVEL_2, null);

        verify(eidasMatchingDatasetUnmarshaller, times(1)).fromAssertion(eidasAssertion);
    }

    @Test
    public void shouldCorrectlyExtractLevelOfAssurance() {
        Assertion eidasAssertion = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();

        LevelOfAssurance loa = eidasAssertionService.extractLevelOfAssuranceFrom(eidasAssertion);

        assertThat(loa).isEqualTo(LEVEL_2);
    }

    @Test
    public void expectedHashContainedInResponseBodyWhenUserIdFactoryIsCalledOnce() {
        String requestId = "requestId";
        String expectedHashed = "a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298";
        TestTranslatedNonMatchingResponseBody expectedNonMatchingResponseBody = new TestTranslatedNonMatchingResponseBody(NonMatchingScenario.IDENTITY_VERIFIED, expectedHashed, LEVEL_2, null);

        Assertion eidasAssertion = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, requestId).buildUnencrypted();

        final String nameId = eidasAssertion.getSubject().getNameID().getValue();
        final String issuerId = eidasAssertion.getIssuer().getValue();

        when(userIdHashFactory.hashId(eq(issuerId), eq(nameId), eq(Optional.of(AuthnContext.LEVEL_2))))
                .thenReturn(expectedHashed);

        TranslatedNonMatchingResponseBody responseBody = eidasAssertionService.translateSuccessResponse(ImmutableList.of(eidasAssertion), "requestId", LEVEL_2, "default-entity-id");

        verify(userIdHashFactory, times(1)).hashId(issuerId,nameId, Optional.of(AuthnContext.LEVEL_2));
        assertThat(responseBody.toString()).contains(expectedNonMatchingResponseBody.getPid());
    }

    @Test(expected = SamlResponseValidationException.class)
    public void shouldThrowAnExceptionIfMultipleAssertionsReceived() {
        Assertion eidasAssertion1 = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();
        Assertion eidasAssertion2 = anAssertionWithAuthnStatement(EIDAS_LOA_SUBSTANTIAL, "requestId").buildUnencrypted();
        eidasAssertionService.translateSuccessResponse(asList(eidasAssertion1, eidasAssertion2), "requestId", LEVEL_2, null);
    }

    @Test
    public void shouldCorrectlyIdentifyCountryAssertions() {
        List<String> resolverEntityIds = asList("ID1", "ID2");
        when(metadataResolverRepository.getResolverEntityIds()).thenReturn(resolverEntityIds);

        Assertion countryAssertion = anAssertion().withIssuer(anIssuer().withIssuerId("ID1").build()).buildUnencrypted();
        Assertion idpAssertion = anAssertion().withIssuer(anIssuer().withIssuerId("ID3").build()).buildUnencrypted();

        assertThat(eidasAssertionService.isCountryAssertion(countryAssertion)).isTrue();
        assertThat(eidasAssertionService.isCountryAssertion(idpAssertion)).isFalse();
    }


    private static AssertionBuilder anAssertionWithAuthnStatement(String authnContext, String inResponseTo) {
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
            .withSubject(anAssertionSubject(inResponseTo))
            .withIssuer(anIssuer().withIssuerId(STUB_COUNTRY_ONE).build())
            .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build());
    }

    private static Subject anAssertionSubject(final String inResponseTo) {
        return aSubject()
            .withSubjectConfirmation(
                aSubjectConfirmation()
                    .withSubjectConfirmationData(
                        aSubjectConfirmationData()
                            .withNotOnOrAfter(DateTime.now())
                            .withInResponseTo(inResponseTo)
                            .build()
                    ).build()
            ).build();
    }
}
