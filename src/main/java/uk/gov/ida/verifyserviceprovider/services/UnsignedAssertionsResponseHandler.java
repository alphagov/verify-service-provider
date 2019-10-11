package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.eidas.CountrySamlResponse;
import uk.gov.ida.saml.core.extensions.eidas.EncryptedAssertionKeys;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.deserializers.StringToOpenSamlObjectTransformer;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.EidasValidatorFactory;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SecretKeyDecryptorFactory;
import uk.gov.ida.saml.security.exception.SamlFailedToDecryptException;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.verifyserviceprovider.validators.InstantValidator;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static uk.gov.ida.saml.security.errors.SamlTransformationErrorFactory.unableToDecrypt;

public class UnsignedAssertionsResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UnsignedAssertionsResponseHandler.class);

    private final EidasValidatorFactory eidasValidatorFactory;
    private final StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer;
    private final InstantValidator instantValidator;
    private final SecretKeyDecryptorFactory secretKeyDecryptorFactory;
    private final EncryptionAlgorithmValidator encryptionAlgorithmValidator;

    public UnsignedAssertionsResponseHandler (
            EidasValidatorFactory eidasValidatorFactory,
            StringToOpenSamlObjectTransformer<Response> stringToResponseTransformer,
            InstantValidator instantValidator,
            SecretKeyDecryptorFactory secretKeyDecryptorFactory,
            EncryptionAlgorithmValidator encryptionAlgorithmValidator
    ) {
        this.eidasValidatorFactory = eidasValidatorFactory;
        this.stringToResponseTransformer = stringToResponseTransformer;
        this.instantValidator = instantValidator;
        this.secretKeyDecryptorFactory = secretKeyDecryptorFactory;
        this.encryptionAlgorithmValidator = encryptionAlgorithmValidator;
    }

    public ValidatedResponse getValidatedResponse(
            Assertion hubResponseAssertion,
            String expectedInResponseTo
    ) {
        ValidatedResponse validatedResponse = eidasValidatorFactory.getValidatedResponse(
                stringToResponseTransformer.apply(
                        getCountryResponseStringFromAssertion(hubResponseAssertion)
                )
        );

        if (!expectedInResponseTo.equals(validatedResponse.getInResponseTo())) {
            throw new SamlResponseValidationException(
                    String.format("Expected InResponseTo to be %s, but was %s", expectedInResponseTo, validatedResponse.getInResponseTo())
            );
        }

        instantValidator.validate(validatedResponse.getIssueInstant(), "Response IssueInstant");

        return validatedResponse;
    }

    public List<Assertion> decryptAssertion(
            ValidatedResponse validatedResponse,
            Assertion hubResponseAssertion
    ) {
        Iterator<String> keysIterator = getEncryptedAssertionKeysFromAssertion(hubResponseAssertion).iterator();

        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            try {
                return getAssertionDecrypter(key).decryptAssertions(validatedResponse);
            } catch (Exception e) {
                if (keysIterator.hasNext()) {
                    LOG.info("Failed to decrypt assertions with key, trying next available", e);
                } else {
                    String message = String.format("Failed to decrypt assertions with any key for response ID %s", validatedResponse.getID());
                    throw new SamlFailedToDecryptException(unableToDecrypt(message), e);
                }
            }
        }
        return new ArrayList<>();
    }

    private AssertionDecrypter getAssertionDecrypter(String key) {
        try {
            return new AssertionDecrypter(
                    encryptionAlgorithmValidator,
                    secretKeyDecryptorFactory.createDecrypter(key)
            );
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new SamlFailedToDecryptException(unableToDecrypt("Unable to create decrypter from encrypted key"), e);
        }
    }

    private String getCountryResponseStringFromAssertion(Assertion hubResponseAssertion) {
        List<Attribute> attributes = hubResponseAssertion.getAttributeStatements().get(0).getAttributes();
        CountrySamlResponse countrySamlResponse = (CountrySamlResponse) attributes.get(0).getAttributeValues().get(0);
        return countrySamlResponse.getValue();
    }

    private List<String> getEncryptedAssertionKeysFromAssertion(Assertion hubResponseAssertion) {
        List<String> keys = new ArrayList<String>();
        hubResponseAssertion
                .getAttributeStatements()
                .get(0)
                .getAttributes()
                .stream()
                .filter(attribute -> attribute.getName().equals(IdaConstants.Eidas_Attributes.UnsignedAssertions.EncryptedSecretKeys.NAME))
                .forEach(attribute -> attribute
                        .getAttributeValues()
                        .stream()
                        .map(value -> (EncryptedAssertionKeys) value)
                        .forEach(value -> keys.add(value.getValue()))
                );
        return keys;
    }
}
