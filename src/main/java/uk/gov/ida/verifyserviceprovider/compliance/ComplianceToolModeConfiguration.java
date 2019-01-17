package uk.gov.ida.verifyserviceprovider.compliance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.bouncycastle.operator.OperatorCreationException;
import uk.gov.ida.verifyserviceprovider.configuration.HubEnvironment;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyHubConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplianceToolModeConfiguration extends VerifyServiceProviderConfiguration {


    private KeysAndCert signingKeysAndCert;
    private KeysAndCert encryptionKeysAndCert;
    private String serviceEntityId;

    public X509Certificate getSigningCertificate() {
        return signingKeysAndCert.getCertificate();
    }

    @JsonCreator
    public ComplianceToolModeConfiguration() {
        super(null,
              null,
              new VerifyHubConfiguration(
                      HubEnvironment.COMPLIANCE_TOOL,
                      null,
                      null
              ),
              null,
              null,
              null,
              Optional.empty(),
              null,
                null);

    }

    public X509Certificate getSamlSigningCertificate() {
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

    @Override
    public List<String> getServiceEntityIds() {
        return Collections.singletonList(serviceEntityId);
    }

    @Override
    public String getHashingEntityId() {
        return serviceEntityId;
    }

    public X509Certificate getEncryptionCertificate() {
        return encryptionKeysAndCert.getCertificate();
    }

    public void setEntityID(String entityID) {
        this.serviceEntityId = entityID;
    }

    public void generateKeys() throws CertificateException, NoSuchAlgorithmException, IOException, OperatorCreationException {
        this.signingKeysAndCert = new KeysAndCert(serviceEntityId);
        this.encryptionKeysAndCert = new KeysAndCert(serviceEntityId);
        this.signingKeysAndCert.generate();
        this.encryptionKeysAndCert.generate();
    }

    public ComplianceToolService createComplianceToolService(Environment environment, String url, Integer timeout) {
        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setConnectionRequestTimeout(Duration.seconds(timeout));
        configuration.setTimeout(Duration.seconds(timeout));
        configuration.setConnectionTimeout(Duration.seconds(timeout));
        Client client = new JerseyClientBuilder(environment).using(configuration).build("Compliance Tool Initiation Client");
        return new ComplianceToolService(client, url, serviceEntityId, getSamlSigningCertificate(), getEncryptionCertificate());
    }
}
