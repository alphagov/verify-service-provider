package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;
import io.dropwizard.validation.ValidationMethod;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.joda.time.Duration;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.NoHashingEntityIdIsProvidedError;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.security.PrivateKey;
import java.util.List;
import java.util.Optional;

public class VerifyServiceProviderConfiguration extends Configuration {

    private static final String NOT_EMPTY_MESSAGE = "may not be empty";

    private List<String> serviceEntityIds;
    private String hashingEntityId;
    private VerifyHubConfiguration verifyHubConfiguration;
    private PrivateKey samlSigningKey;
    private PrivateKey samlPrimaryEncryptionKey;
    private PrivateKey samlSecondaryEncryptionKey;
    private Optional<MsaMetadataConfiguration> msaMetadata;
    private Duration clockSkew;
    private Optional<EuropeanIdentityConfiguration> europeanIdentity;

    protected VerifyServiceProviderConfiguration() {
    }

    public VerifyServiceProviderConfiguration(
            @JsonProperty("serviceEntityIds") @NotNull @Size(min = 1, message = NOT_EMPTY_MESSAGE) @Valid List<String> serviceEntityIds,
            @JsonProperty("hashingEntityId") @Valid String hashingEntityId,
            @JsonProperty("verifyHubConfiguration") @NotNull @Valid VerifyHubConfiguration verifyHubConfiguration,
            @JsonProperty("samlSigningKey") @NotNull @Valid @JsonDeserialize(using = PrivateKeyDeserializer.class) PrivateKey samlSigningKey,
            @JsonProperty("samlPrimaryEncryptionKey") @NotNull @Valid @JsonDeserialize(using = PrivateKeyDeserializer.class) PrivateKey samlPrimaryEncryptionKey,
            @JsonProperty("samlSecondaryEncryptionKey") @Valid @JsonDeserialize(using = PrivateKeyDeserializer.class) PrivateKey samlSecondaryEncryptionKey,
            @JsonProperty("msaMetadata") @NotNull @UnwrapValidatedValue @Valid Optional<MsaMetadataConfiguration> msaMetadata,
            @JsonProperty("clockSkew") @NotNull @Valid Duration clockSkew,
            @JsonProperty("europeanIdentity") @Valid @UnwrapValidatedValue Optional<EuropeanIdentityConfiguration> europeanIdentity) {
        this.serviceEntityIds = serviceEntityIds;
        this.hashingEntityId = hashingEntityId;
        this.verifyHubConfiguration = verifyHubConfiguration;
        this.samlSigningKey = samlSigningKey;
        this.samlPrimaryEncryptionKey = samlPrimaryEncryptionKey;
        this.samlSecondaryEncryptionKey = samlSecondaryEncryptionKey;
        this.msaMetadata = msaMetadata;
        this.clockSkew = clockSkew;
        this.europeanIdentity = europeanIdentity;
        this.europeanIdentity.ifPresent(eid -> eid.setEnvironment(verifyHubConfiguration.getHubEnvironment()));
    }

    public List<String> getServiceEntityIds() {
        return serviceEntityIds;
    }

    public String getHashingEntityId() {
        if (hashingEntityId != null) {
            return hashingEntityId;
        }

        if (getServiceEntityIds().size() == 1) {
            return getServiceEntityIds().get(0);
        }

        throw new NoHashingEntityIdIsProvidedError("No HashingEntityId is provided");
    }

    public URI getHubSsoLocation() {
        return verifyHubConfiguration.getHubSsoLocation();
    }

    public PrivateKey getSamlSigningKey() {
        return samlSigningKey;
    }

    public PrivateKey getSamlPrimaryEncryptionKey() {
        return samlPrimaryEncryptionKey;
    }

    public PrivateKey getSamlSecondaryEncryptionKey() {
        return samlSecondaryEncryptionKey;
    }

    public Optional<MetadataResolverConfiguration> getMsaMetadata() {
        return msaMetadata.map((msaMetadataConfiguration -> msaMetadataConfiguration));
    }

    public HubMetadataConfiguration getVerifyHubMetadata() {
        return verifyHubConfiguration.getHubMetadataConfiguration();
    }

    public Duration getClockSkew() {
        return clockSkew;
    }

    public Optional<EuropeanIdentityConfiguration> getEuropeanIdentity() {
        return europeanIdentity;
    }

    @ValidationMethod(message = "eIDAS and MSA support cannot be set together. The VSP's eIDAS support is only available when it operates without the MSA")
    @JsonIgnore
    public boolean isNotMsaAndEidas() {
        return !(msaMetadata.isPresent() && europeanIdentity.isPresent());
    }
}
