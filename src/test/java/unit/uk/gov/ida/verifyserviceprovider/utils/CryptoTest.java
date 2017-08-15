package unit.uk.gov.ida.verifyserviceprovider.utils;

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.ida.verifyserviceprovider.utils.Crypto.publicKeyFromPrivateKey;

public class CryptoTest {
    @Test
    public void publicKeyFromPrivateKeyConvertsRSAKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey convertedKey = publicKeyFromPrivateKey(keyPair.getPrivate());

        assertThat(convertedKey.getEncoded()).isNotNull();
    }

    @Test
    public void publicKeyFromPrivateKeyCreatesCorrectPublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        KeyPair wrongKeyPair = keyPairGenerator.generateKeyPair();

        PublicKey convertedKey = publicKeyFromPrivateKey(keyPair.getPrivate());

        assertThat(convertedKey.getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
        assertThat(convertedKey.getEncoded()).isNotEqualTo(wrongKeyPair.getPublic().getEncoded());
    }

    @Test
    public void publicKeyFromPrivateKeyThrowsWhenNotRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        assertThatThrownBy(() -> {
            publicKeyFromPrivateKey(keyPair.getPrivate());
        }).isInstanceOf(RuntimeException.class)
        .hasMessage("Private key must be RSA format");
    }

}