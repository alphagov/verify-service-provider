package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.deserializers.OpenSamlXMLObjectUnmarshaller;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.deserializers.parser.SamlObjectParser;
import uk.gov.ida.saml.deserializers.validators.Base64StringDecoder;
import uk.gov.ida.saml.deserializers.validators.NotNullSamlStringValidator;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;
import uk.gov.ida.verifyserviceprovider.services.AssertionService;
import uk.gov.ida.verifyserviceprovider.services.MatchingAssertionService;
import uk.gov.ida.verifyserviceprovider.services.NonMatchingAssertionService;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;
import uk.gov.ida.verifyserviceprovider.utils.DateTimeComparator;
import uk.gov.ida.verifyserviceprovider.validators.AssertionValidator;
import uk.gov.ida.verifyserviceprovider.validators.AudienceRestrictionValidator;
import uk.gov.ida.verifyserviceprovider.validators.ConditionsValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;
import uk.gov.ida.verifyserviceprovider.validators.ResponseSizeValidator;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;
import uk.gov.ida.verifyserviceprovider.validators.TimeRestrictionValidator;

import java.security.KeyPair;
import java.util.List;

public class ResponseFactory {

    private static final NotNullSamlStringValidator notNullSamlStringValidator = new NotNullSamlStringValidator();
    private static final Base64StringDecoder base64StringDecoder = new Base64StringDecoder();
    private static final ResponseSizeValidator responseSizeValidator = new ResponseSizeValidator();
    private static final SamlObjectParser samlObjectParser = new SamlObjectParser();
    private static final OpenSamlXMLObjectUnmarshaller<Response> responseOpenSamlXMLObjectUnmarshaller = new OpenSamlXMLObjectUnmarshaller<>(samlObjectParser);
    private static final EncryptionAlgorithmValidator encryptionAlgorithmValidator = new EncryptionAlgorithmValidator();
    private static final DecrypterFactory decrypterFactory = new DecrypterFactory();

    private List<KeyPair> encryptionKeyPairs;

    public ResponseFactory(List<KeyPair> encryptionKeyPairs) {
        this.encryptionKeyPairs = encryptionKeyPairs;
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
        List<Credential> decryptingCredentials = new IdaKeyStoreCredentialRetriever(createEncryptionKeyStore()).getDecryptingCredentials();
        return new AssertionDecrypter(
            encryptionAlgorithmValidator,
            decrypterFactory.createDecrypter(decryptingCredentials)
        );
    }

    public ResponseService createResponseService(
        ExplicitKeySignatureTrustEngine hubSignatureTrustEngine,
        AssertionService assertionService,
        DateTimeComparator dateTimeComparator
    ) {
        AssertionDecrypter assertionDecrypter = createAssertionDecrypter();
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator(hubSignatureTrustEngine);

        return new ResponseService(
            createStringToResponseTransformer(),
            assertionDecrypter,
            assertionService,
            new SamlResponseSignatureValidator(new SamlMessageSignatureValidator(metadataBackedSignatureValidator)),
            new InstantValidator(dateTimeComparator)
        );
    }

    public MatchingAssertionService createMatchingAssertionService(
        ExplicitKeySignatureTrustEngine signatureTrustEngine,
        DateTimeComparator dateTimeComparator
    ) {
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator(signatureTrustEngine);
        SamlMessageSignatureValidator samlMessageSignatureValidator = new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
        TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);

        SamlAssertionsSignatureValidator assertionsSignatureValidator = new SamlAssertionsSignatureValidator(samlMessageSignatureValidator);
        AssertionValidator assertionValidator = new AssertionValidator(
            new InstantValidator(dateTimeComparator),
            new SubjectValidator(timeRestrictionValidator),
            new ConditionsValidator(timeRestrictionValidator, new AudienceRestrictionValidator())
        );

        return new MatchingAssertionService(
            assertionsSignatureValidator,
            assertionValidator
        );
    }

    public NonMatchingAssertionService createNonMatchingAssertionService(ExplicitKeySignatureTrustEngine signatureTrustEngine,
                                                                         DateTimeComparator dateTimeComparator) {

        MetadataBackedSignatureValidator metadataBackedSignatureValidator = createMetadataBackedSignatureValidator(signatureTrustEngine);
        SamlMessageSignatureValidator samlMessageSignatureValidator = new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
        TimeRestrictionValidator timeRestrictionValidator = new TimeRestrictionValidator(dateTimeComparator);

        SamlAssertionsSignatureValidator assertionsSignatureValidator = new SamlAssertionsSignatureValidator(samlMessageSignatureValidator);
        AssertionValidator assertionValidator = new AssertionValidator(
                new InstantValidator(dateTimeComparator),
                new SubjectValidator(timeRestrictionValidator),
                new ConditionsValidator(timeRestrictionValidator, new AudienceRestrictionValidator())
        );
        return new NonMatchingAssertionService(assertionsSignatureValidator, assertionValidator);
    }

    private MetadataBackedSignatureValidator createMetadataBackedSignatureValidator(ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine) {
        return MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
    }

    private IdaKeyStore createEncryptionKeyStore() {
        return new IdaKeyStore(null, encryptionKeyPairs);
    }
}
