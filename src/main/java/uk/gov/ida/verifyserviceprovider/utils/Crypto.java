package uk.gov.ida.verifyserviceprovider.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

public class Crypto {

    public static PublicKey publicKeyFromPrivateKey(PrivateKey privateKey) {
        try {
            RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (ClassCastException ex) {
            throw new RuntimeException("Private key must be RSA format");
        } catch (Exception ex) {
            throw new RuntimeException("Could not get public key from private key");
        }
    }
}
