package uk.gov.ida.verifyserviceprovider.compliance;

import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.server.SimpleServerFactory;
import org.bouncycastle.operator.OperatorCreationException;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.UUID;

public class ComplianceToolModeConfigurationFactory implements ConfigurationFactory<VerifyServiceProviderConfiguration> {

    private final int port;
    private final String bindHost;


    ComplianceToolModeConfigurationFactory(int port, String bindHost) {
        this.port = port;
        this.bindHost = bindHost;
    }

    private ComplianceToolModeConfiguration createComplianceToolModeConfiguration() throws IOException {
        String serviceEntityId = UUID.randomUUID().toString();
        KeysAndCert signingKeysAndCert = createKeysAndCert(serviceEntityId);
        KeysAndCert encryptionKeysAndCert = createKeysAndCert(serviceEntityId);

        ComplianceToolModeConfiguration complianceToolModeConfiguration = new ComplianceToolModeConfiguration(serviceEntityId, signingKeysAndCert, encryptionKeysAndCert);

        HttpConnectorFactory httpConnectorFactory = new HttpConnectorFactory();
        httpConnectorFactory.setPort(port);
        httpConnectorFactory.setBindHost(bindHost);
        SimpleServerFactory simpleServerFactory = new SimpleServerFactory();
        simpleServerFactory.setApplicationContextPath("/");
        simpleServerFactory.setConnector(httpConnectorFactory);
        complianceToolModeConfiguration.setServerFactory(simpleServerFactory);
        complianceToolModeConfiguration.setLoggingFactory(new DefaultLoggingFactory());

        return complianceToolModeConfiguration;
    }

    private KeysAndCert createKeysAndCert(String serviceEntityId) throws IOException {
        KeysAndCert keysAndCert = new KeysAndCert(serviceEntityId);
        try {
            keysAndCert.generate();
        } catch (CertificateException | NoSuchAlgorithmException | OperatorCreationException e) {
            throw new RuntimeException(e);
        }
        return keysAndCert;
    }

    @Override
    public VerifyServiceProviderConfiguration build(ConfigurationSourceProvider provider, String path) throws IOException {
        return createComplianceToolModeConfiguration();
    }

    @Override
    public VerifyServiceProviderConfiguration build() throws IOException {
        return createComplianceToolModeConfiguration();
    }

}
