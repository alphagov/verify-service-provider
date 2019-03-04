package uk.gov.ida.verifyserviceprovider.compliance;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import uk.gov.ida.verifyserviceprovider.configuration.HubEnvironment;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyHubConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.client.Client;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static java.util.Arrays.asList;


public class ComplianceToolModeConfiguration extends VerifyServiceProviderConfiguration {
    private final String serviceEntityId;
    private KeysAndCert signingKeysAndCert;
    private KeysAndCert encryptionKeysAndCert;

    public ComplianceToolModeConfiguration(String serviceEntityId, KeysAndCert signingKeysAndCert, KeysAndCert encryptionKeysAndCert) {
        super(asList(serviceEntityId),
              serviceEntityId,
              new VerifyHubConfiguration(HubEnvironment.COMPLIANCE_TOOL),
              signingKeysAndCert.getPrivate(),
              encryptionKeysAndCert.getPrivate(),
              encryptionKeysAndCert.getPrivate(),
              Optional.empty(),
              org.joda.time.Duration.standardMinutes(2),
              Optional.empty());

        this.serviceEntityId = serviceEntityId;
        this.signingKeysAndCert = signingKeysAndCert;
        this.encryptionKeysAndCert = encryptionKeysAndCert;
    }

    public X509Certificate getSigningCertificate() {
        return signingKeysAndCert.getCertificate();
    }

    @Override
    public PrivateKey getSamlSigningKey() {
        return signingKeysAndCert.getPrivate();
    }

    @Override
    public PrivateKey getSamlPrimaryEncryptionKey() {
        return encryptionKeysAndCert.getPrivate();
    }

    @Override
    public PrivateKey getSamlSecondaryEncryptionKey() {
        return encryptionKeysAndCert.getPrivate();
    }


    public X509Certificate getEncryptionCertificate() {
        return encryptionKeysAndCert.getCertificate();
    }


    public ComplianceToolClient createComplianceToolService(Environment environment, String url, Integer timeout) {
        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setConnectionRequestTimeout(Duration.seconds(timeout));
        configuration.setTimeout(Duration.seconds(timeout));
        configuration.setConnectionTimeout(Duration.seconds(timeout));
        Client client = new JerseyClientBuilder(environment).using(configuration).build("Compliance Tool Initiation Client");
        return new ComplianceToolClient(client, url, serviceEntityId, getSigningCertificate(), getEncryptionCertificate());
    }
}
