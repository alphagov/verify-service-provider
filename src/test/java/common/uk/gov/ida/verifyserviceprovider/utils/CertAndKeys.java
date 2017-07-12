package common.uk.gov.ida.verifyserviceprovider.utils;


import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class CertAndKeys {

    private static final int KEY_SIZE = 2048;
    private static final String KEY_TYPE = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";
    private static final String DISTINGUISHED_NAME = "CN=My Application,O=My Organisation,L=My City,C=DE";
    private static final long ONE_YEAR_CERT_EXPIRY = (long) 365 * 24 * 60 * 60;

    public final RSAPublicKey publicKey;
    public final RSAPrivateKey privateKey;
    public final X509Certificate certificate;

    private CertAndKeys(RSAPublicKey publicKey, RSAPrivateKey privateKey, X509Certificate certificate) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public static CertAndKeys generate() throws Exception {
        CertAndKeyGen certGen = new CertAndKeyGen(KEY_TYPE, SIGNATURE_ALGORITHM);
        certGen.generate(KEY_SIZE);

        X509Certificate certificate = certGen.getSelfCertificate(new X500Name(DISTINGUISHED_NAME), ONE_YEAR_CERT_EXPIRY);
        RSAPrivateKey privateKey = (RSAPrivateKey) certGen.getPrivateKey();
        RSAPublicKey publicKey = (RSAPublicKey) certGen.getPublicKey();

        return new CertAndKeys(publicKey, privateKey, certificate);
    }
}
