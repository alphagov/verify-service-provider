package uk.gov.ida.verifyserviceprovider.factories;

import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.saml.security.EncryptionCredentialResolver;

public class EncrypterFactory extends uk.gov.ida.saml.security.EncrypterFactory {

    private final EncryptionCredentialResolver encryptionCredentialResolver;
    private final String hubEntityId;

    public EncrypterFactory(EncryptionCredentialResolver encryptionCredentialResolver, String hubEntityId) {
        this.encryptionCredentialResolver = encryptionCredentialResolver;
        this.hubEntityId = hubEntityId;
    }

    public Encrypter createEncrypter() {
        Credential credential = encryptionCredentialResolver.getEncryptingCredential(hubEntityId);
        return super.createEncrypter(credential);
    }
}
