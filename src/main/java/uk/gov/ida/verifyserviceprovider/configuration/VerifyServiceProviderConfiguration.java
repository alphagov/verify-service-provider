package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.security.PrivateKey;

public class VerifyServiceProviderConfiguration extends Configuration {

    public static final String NOT_EMPTY_MESSAGE = "may not be empty";

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String hubSsoLocation;

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String hubEntityId;

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String msaEntityId;

    @JsonProperty
    @NotNull
    @Valid
    private URI msaMetadataUrl;

    @JsonProperty
    @NotNull
    @Valid
    private URI hubMetadataUrl;

    @JsonProperty
    @NotNull
    @Size(min = 1, message = NOT_EMPTY_MESSAGE)
    @Valid
    private String secureTokenKey;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSigningKey;

    @JsonProperty
    @NotNull
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlPrimaryEncryptionKey;

    @JsonProperty
    @Valid
    @JsonDeserialize(using = PrivateKeyDeserializer.class)
    private PrivateKey samlSecondaryEncryptionKey;

    public String getHubSsoLocation() {
        return hubSsoLocation;
    }

    public String getHubEntityId() {
        return hubEntityId;
    }

    public URI getMsaMetadataUrl() {
        return msaMetadataUrl;
    }

    public String getSecureTokenKey() {
        return secureTokenKey;
    }

    public String getMsaEntityId() {
        return msaEntityId;
    }

    public URI getHubMetadataUrl() {
        return hubMetadataUrl;
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
}
