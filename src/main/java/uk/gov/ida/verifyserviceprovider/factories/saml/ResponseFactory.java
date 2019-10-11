package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.core.domain.AddressFactory;
import uk.gov.ida.saml.core.transformers.EidasMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.VerifyMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.validation.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.core.validation.conditions.AudienceRestrictionValidator;
import uk.gov.ida.saml.deserializers.OpenSamlXMLObjectUnmarshaller;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.deserializers.parser.SamlObjectParser;
import uk.gov.ida.saml.deserializers.validators.Base64StringDecoder;
import uk.gov.ida.saml.deserializers.validators.NotNullSamlStringValidator;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.EidasValidatorFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;
import uk.gov.ida.verifyserviceprovider.configuration.EuropeanIdentityConfiguration;
import uk.gov.ida.verifyserviceprovider.mappers.MatchingDatasetToNonMatchingAttributesMapper;
import uk.gov.ida.verifyserviceprovider.services.AssertionClassifier;
import uk.gov.ida.verifyserviceprovider.services.AssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.EidasUnsignedAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.IdentityResponderCodeTranslator;
import uk.gov.ida.verifyserviceprovider.services.MatchingAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.services.MatchingResponderCodeTranslator;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;
import uk.gov.ida.verifyserviceprovider.services.UnsignedAssertionsResponseHandler;
import uk.gov.ida.verifyserviceprovider.services.VerifyAssertionTranslator;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.LevelOfAssuranceValidator;
import uk.gov.ida.verifyserviceprovider.validators.ResponseSizeValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;
import uk.gov.ida.verifyserviceprovider.validators.TimeRestrictionValidator;

import java.security.KeyPair;
import java.util.List;
import java.util.Optional;

public class ResponseFactory {

    private static final NotNullSamlStringValidator notNullSamlStringValidator = new NotNullSamlStringValidator();
    private static final Base64StringDecoder base64StringDecoder = new Base64StringDecoder();
    private static final ResponseSizeValidator responseSizeValidator = new ResponseSizeValidator();
    private static final SamlObjectParser samlObjectParser = new SamlObjectParser();
    private static final OpenSamlXMLObjectUnmarshaller<Response> responseOpenSamlXMLObjectUnmarshaller = new OpenSamlXMLObjectUnmarshaller<>(samlObjectParser);
    private static final EncryptionAlgorithmValidator encryptionAlgorithmValidator = new EncryptionAlgorithmValidator();
    private static final DecrypterFactory decrypterFactory = new DecrypterFactory();

    private List<KeyPair> encryptionKeyPairs;
    private final IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever;

    public ResponseFactory(List<KeyPair> encryptionKeyPairs) {
        this.encryptionKeyPairs = encryptionKeyPairs;
        this.idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(createEncryptionKeyStore());
    }

    public static StringToOpenSamlObjectTransformer<Response> createStringToResponseTransformer() {
        return new StringToOpenSamlObjectTransformer<>(
                notNullSamlStringValidator,
                base64StringDecoder,
                responseSizeValidator,
                responseOpenSamlXMLObjectUnmarshaller
        );
    }

    public AssertionDecrypter createAssertionDecrypter() {
        List<Credential> decryptingCredentials = idaKeyStoreCredentialRetriever.getDecryptingCredentials();
        return new AssertionDecrypter(
                encryptionAlgorithmValidator,
                decrypterFactory.createDecrypter(decryptingCredentials)
        );
    }

    public ResponseService createMatchingResponseService(
            ExplicitKeySignatureTrustEngine hubSignatureTrustEngine,
            AssertionTranslator matchingAssertionTranslator,
            DateTimeComparator dateTimeComparator
    ) {
        AssertionDecrypter assertionDecrypter = createAssertionDecrypter();
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator(hubSignatureTrustEngine);

        return new ResponseService(
            createStringToResponseTransformer(),
            assertionDecrypter,
            matchingAssertionTranslator,
            new SamlResponseSignatureValidator(new SamlMessageSignatureValidator(metadataBackedSignatureValidator)),
            new InstantValidator(dateTimeComparator),
            new MatchingResponderCodeTranslator(),
            Optional.empty()
        );
    }

    public ResponseService createNonMatchingResponseService(
            ExplicitKeySignatureTrustEngine hubSignatureTrustEngine,
            AssertionTranslator nonMatchingAssertionTranslator,
            DateTimeComparator dateTimeComparator,
            Optional<UnsignedAssertionsResponseHandler> unsignedAssertionsResponseHandler
    ) {
        AssertionDecrypter assertionDecrypter = createAssertionDecrypter();
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator(hubSignatureTrustEngine);
        StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer = createStringToResponseTransformer();

        return new ResponseService(
            stringToResponseTransformer,
            assertionDecrypter,
            nonMatchingAssertionTranslator,
            new SamlResponseSignatureValidator(new SamlMessageSignatureValidator(metadataBackedSignatureValidator)),
            new InstantValidator(dateTimeComparator),
            new IdentityResponderCodeTranslator(),
            unsignedAssertionsResponseHandler
        );
    }

    public MatchingAssertionTranslator createMsaAssertionService(
            ExplicitKeySignatureTrustEngine signatureTrustEngine,
            SignatureValidatorFactory signatureValidatorFactory,
            DateTimeComparator dateTimeComparator
    ) {
        TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);

        SamlAssertionsSignatureValidator signatureValidator = signatureValidatorFactory.getSignatureValidator(signatureTrustEngine);
        AssertionValidator assertionValidator = new AssertionValidator(
                new InstantValidator(dateTimeComparator),
                new SubjectValidator(timeRestrictionValidator),
                new ConditionsValidator(timeRestrictionValidator, new AudienceRestrictionValidator())
        );

        return new MatchingAssertionTranslator(
                assertionValidator,
                new LevelOfAssuranceValidator(),
                signatureValidator
        );
    }

    public VerifyAssertionTranslator createVerifyIdpAssertionService(ExplicitKeySignatureTrustEngine signatureTrustEngine,
                                                                     SignatureValidatorFactory signatureValidatorFactory,
                                                                     DateTimeComparator dateTimeComparator,
                                                                     String hashingEntityId) {

        TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);

        return new VerifyAssertionTranslator(
                signatureValidatorFactory.getSignatureValidator(signatureTrustEngine),
                new SubjectValidator(timeRestrictionValidator),
                new AssertionAttributeStatementValidator(),
                new VerifyMatchingDatasetUnmarshaller(new AddressFactory()),
                new AssertionClassifier(),
                new MatchingDatasetToNonMatchingAttributesMapper(),
                new LevelOfAssuranceValidator(),
                new UserIdHashFactory(hashingEntityId)
            );
    }

    public EidasAssertionTranslator createEidasAssertionService(
            DateTimeComparator dateTimeComparator,
            EidasMetadataResolverRepository eidasMetadataResolverRepository,
            EuropeanIdentityConfiguration europeanIdentityConfiguration,
            String hashingEntityId
    ) {
        TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);
        AudienceRestrictionValidator audienceRestrictionValidator = new AudienceRestrictionValidator();

        return new EidasAssertionTranslator(
                new SubjectValidator(timeRestrictionValidator),
                new EidasMatchingDatasetUnmarshaller(),
                new MatchingDatasetToNonMatchingAttributesMapper(),
                new InstantValidator(dateTimeComparator),
                new ConditionsValidator(timeRestrictionValidator, audienceRestrictionValidator),
                new LevelOfAssuranceValidator(),
                eidasMetadataResolverRepository,
                new SignatureValidatorFactory(),
                europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds(),
                new UserIdHashFactory(hashingEntityId)
        );
    }

    public EidasUnsignedAssertionTranslator createEidasUnsignedAssertionService(
            DateTimeComparator dateTimeComparator,
            EidasMetadataResolverRepository eidasMetadataResolverRepository,
            EuropeanIdentityConfiguration europeanIdentityConfiguration,
            String hashingEntityId
    ) {
        TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);
        AudienceRestrictionValidator audienceRestrictionValidator = new AudienceRestrictionValidator();

        return new EidasUnsignedAssertionTranslator(
                new SubjectValidator(timeRestrictionValidator),
                new EidasMatchingDatasetUnmarshaller(),
                new MatchingDatasetToNonMatchingAttributesMapper(),
                new InstantValidator(dateTimeComparator),
                new ConditionsValidator(timeRestrictionValidator, audienceRestrictionValidator),
                new LevelOfAssuranceValidator(),
                eidasMetadataResolverRepository,
                europeanIdentityConfiguration.getAllAcceptableHubConnectorEntityIds(),
                new UserIdHashFactory(hashingEntityId)
        );
    }

    private MetadataBackedSignatureValidator createMetadataBackedSignatureValidator(ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine) {
        return MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
    }

    private IdaKeyStore createEncryptionKeyStore() {
        return new IdaKeyStore(null, encryptionKeyPairs);
    }
}
