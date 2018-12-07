package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;
import org.joda.time.Duration;
import uk.gov.ida.verifyserviceprovider.exceptions.NoHashingEntityIdIsProvidedError;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.security.PrivateKey;
import java.util.List;

public class VerifyServiceProviderConfiguration extends Configuration {

    public static final String NOT_EMPTY_MESSAGE = "may not be empty";
    private List<String> serviceEntityIds;
    private String hashingEntityId;
    private VerifyHubConfiguration verifyHubConfiguration;
    private PrivateKey samlSigningKey;
    private PrivateKey samlPrimaryEncryptionKey;
    private PrivateKey samlSecondaryEncryptionKey;
    private MsaMetadataConfiguration msaMetadata;
    private Duration clockSkew;
    private EuropeanIdentityConfiguration europeanIdentity;

    @JsonCreator
    public VerifyServiceProviderConfiguration(
        @JsonProperty("serviceEntityIds") @NotNull @Size(min = 1, message = NOT_EMPTY_MESSAGE) @Valid List<String> serviceEntityIds,
        @JsonProperty("hashingEntityId") @Valid String hashingEntityId,
        @JsonProperty("verifyHubConfiguration") @NotNull @Valid VerifyHubConfiguration verifyHubConfiguration,
        @JsonProperty("samlSigningKey") @NotNull @Valid @JsonDeserialize(using = PrivateKeyDeserializer.class) PrivateKey samlSigningKey,
        @JsonProperty("samlPrimaryEncryptionKey") @NotNull @Valid @JsonDeserialize(using = PrivateKeyDeserializer.class) PrivateKey samlPrimaryEncryptionKey,
        @JsonProperty("samlSecondaryEncryptionKey") @Valid @JsonDeserialize(using = PrivateKeyDeserializer.class) PrivateKey samlSecondaryEncryptionKey,
        @JsonProperty("msaMetadata") @NotNull @Valid MsaMetadataConfiguration msaMetadata,
        @JsonProperty("clockSkew") @NotNull @Valid Duration clockSkew,
        @JsonProperty("europeanIdentity") @Valid EuropeanIdentityConfiguration europeanIdentity
    ) {
        this.serviceEntityIds = serviceEntityIds;
        this.hashingEntityId = hashingEntityId;
        this.verifyHubConfiguration = verifyHubConfiguration;
        this.samlSigningKey = samlSigningKey;
        this.samlPrimaryEncryptionKey = samlPrimaryEncryptionKey;
        this.samlSecondaryEncryptionKey = samlSecondaryEncryptionKey;
        this.msaMetadata = msaMetadata;
        this.clockSkew = clockSkew;
        this.europeanIdentity = europeanIdentity;
    }

    public List<String> getServiceEntityIds() {
        return serviceEntityIds;
    }

    public String getHashingEntityId() {
        if(hashingEntityId != null) {
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

    public MsaMetadataConfiguration getMsaMetadata() {
        return msaMetadata;
    }

    public HubMetadataConfiguration getVerifyHubMetadata() {
        return verifyHubConfiguration.getHubMetadataConfiguration();
    }

    public Duration getClockSkew() {
        return clockSkew;
    }

    public EuropeanIdentityConfiguration getEuropeanIdentity() {
        return europeanIdentity;
    }
}
