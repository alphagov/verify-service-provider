package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.saml.deserializers.OpenSamlXMLObjectUnmarshaller;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.deserializers.parser.SamlObjectParser;
import uk.gov.ida.saml.deserializers.validators.Base64StringDecoder;
import uk.gov.ida.saml.deserializers.validators.NotNullSamlStringValidator;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.verifyserviceprovider.dto.NotImplementedPublicKey;
import uk.gov.ida.verifyserviceprovider.validators.ResponseSizeValidator;

import java.security.KeyPair;
import java.security.PrivateKey;

import static java.util.Arrays.asList;

public class ResponseFactory {

    private final PrivateKey samlPrimaryEncryptionKey;
    private final PrivateKey samlSecondaryEncryptionKey;

    public ResponseFactory(PrivateKey samlPrimaryEncryptionKey, PrivateKey samlSecondaryEncryptionKey) {
        this.samlPrimaryEncryptionKey = samlPrimaryEncryptionKey;
        this.samlSecondaryEncryptionKey = samlSecondaryEncryptionKey;
    }

    public static StringToOpenSamlObjectTransformer<Response> createStringToResponseTransformer() {
        return new StringToOpenSamlObjectTransformer<>(
            new NotNullSamlStringValidator(),
            new Base64StringDecoder(),
            new ResponseSizeValidator(),
            new OpenSamlXMLObjectUnmarshaller<>(new SamlObjectParser())
        );
    }

    public AssertionDecrypter createAssertionDecrypter() {
        return new AssertionDecrypter(
            new IdaKeyStoreCredentialRetriever(createKeyStore()),
            new EncryptionAlgorithmValidator(),
            new DecrypterFactory()
        );
    }

    private IdaKeyStore createKeyStore() {
        KeyPair primaryEncryptionKeyPair = new KeyPair(new NotImplementedPublicKey(samlPrimaryEncryptionKey), samlPrimaryEncryptionKey);
        KeyPair secondaryEncryptionKeyPair = new KeyPair(new NotImplementedPublicKey(samlSecondaryEncryptionKey), samlSecondaryEncryptionKey);
        return new IdaKeyStore(null, asList(primaryEncryptionKeyPair, secondaryEncryptionKeyPair));
    }
}
