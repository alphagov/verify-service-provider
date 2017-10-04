package uk.gov.ida.verifyserviceprovider.factories;

import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.BasicCredential;
import uk.gov.ida.verifyserviceprovider.metadata.MetadataPublicKeyExtractor;

public class EncrypterFactory extends uk.gov.ida.saml.security.EncrypterFactory {

    private final MetadataPublicKeyExtractor metadataPublicKeyExtractor;

    public EncrypterFactory(MetadataPublicKeyExtractor metadataPublicKeyExtractor) {
        this.metadataPublicKeyExtractor = metadataPublicKeyExtractor;
    }

    public Encrypter createEncrypter() {
        BasicCredential credential = new BasicCredential(metadataPublicKeyExtractor.getEncryptionPublicKey());
        return super.createEncrypter(credential);
    }
}
