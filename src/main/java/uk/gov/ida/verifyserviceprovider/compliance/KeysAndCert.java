package uk.gov.ida.verifyserviceprovider.compliance;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

class KeysAndCert {
    private X509Certificate certificate;
    private String subject;
    private KeyPair keyPair;

    KeysAndCert(String subject) {
        this.subject = subject;
    }

    public PublicKey getPublic() {
        return getKeyPair().getPublic();
    }

    public PrivateKey getPrivate() {
        return getKeyPair().getPrivate();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void generate() throws NoSuchAlgorithmException, IOException, CertificateException, OperatorCreationException {
        KeyPairGenerator sha256WithRSA = KeyPairGenerator.getInstance("RSA");

        sha256WithRSA.initialize(4096);
        KeyPair keyPair = sha256WithRSA.generateKeyPair();
        this.keyPair = keyPair;
        this.certificate = selfSign(keyPair, subject);

    }

    public static X509Certificate selfSign(KeyPair keyPair, String subjectCn) throws OperatorCreationException, CertificateException, IOException
    {
        Provider bcProvider = new BouncyCastleProvider();

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        X500Name dnName = new X500Name(String.format("cn=%s,o=verify-service-provider,l=gov,c=uk", subjectCn));
        BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1); // <-- 1 Yr validity

        Date endDate = calendar.getTime();

        String signatureAlgorithm = "SHA256WithRSA"; // <-- Use appropriate signature algorithm based on your keyPair algorithm.

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

        // Extensions --------------------------

        // Basic Constraints
        BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity

        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        // -------------------------------------

        return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
    }

}
