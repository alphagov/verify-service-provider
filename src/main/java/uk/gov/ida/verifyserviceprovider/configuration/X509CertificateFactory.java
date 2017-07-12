package uk.gov.ida.verifyserviceprovider.configuration;

import com.google.common.base.Throwables;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

public class X509CertificateFactory {

    public X509Certificate createCertificate(String partialCert) {
        java.security.cert.CertificateFactory certificateFactory;
        try {
            certificateFactory = java.security
                .cert
                .CertificateFactory
                .getInstance("X.509");

            String fullCert;
            if (partialCert.contains("-----BEGIN CERTIFICATE-----")) {
                fullCert = partialCert;
            } else {
                fullCert = MessageFormat.format(
                    "-----BEGIN CERTIFICATE-----\n{0}\n-----END CERTIFICATE-----",
                    partialCert.trim()
                );
            }

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullCert.getBytes(StandardCharsets.UTF_8));
            return (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
        } catch (CertificateException e) {
            throw Throwables.propagate(e);
        }
    }

}
