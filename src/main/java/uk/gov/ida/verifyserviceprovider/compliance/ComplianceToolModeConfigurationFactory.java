package uk.gov.ida.verifyserviceprovider.compliance;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import org.bouncycastle.operator.OperatorCreationException;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.validation.Validator;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ComplianceToolModeConfigurationFactory implements ConfigurationFactory<VerifyServiceProviderConfiguration> {

    private final String entityId;
    private YamlConfigurationFactory<ComplianceToolModeConfiguration> yamlConfigurationFactory;


    ComplianceToolModeConfigurationFactory(String entityId, Validator validator, ObjectMapper objectMapper, String propertyPrefix) {
        this.entityId = entityId;
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        yamlConfigurationFactory = new YamlConfigurationFactory<>(ComplianceToolModeConfiguration.class, validator, objectMapper, propertyPrefix);
    }

    @Override
    public VerifyServiceProviderConfiguration build(ConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException {
        ComplianceToolModeConfiguration configuration = yamlConfigurationFactory.build(provider, path);
        addAdditionalConfig(configuration);
        return configuration;
    }

    @Override
    public VerifyServiceProviderConfiguration build() throws IOException, ConfigurationException {
        ComplianceToolModeConfiguration configuration = yamlConfigurationFactory.build();
        addAdditionalConfig(configuration);
        return configuration;
    }

    private void addAdditionalConfig(ComplianceToolModeConfiguration configuration) throws IOException {
        try {
            configuration.setEntityID(entityId);
            configuration.generateKeys();
        } catch (CertificateException | NoSuchAlgorithmException | OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }
}
