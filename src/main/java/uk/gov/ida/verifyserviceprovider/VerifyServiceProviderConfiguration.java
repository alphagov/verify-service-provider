package uk.gov.ida.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class VerifyServiceProviderConfiguration extends Configuration {

    @JsonProperty
    @NotNull
    @Valid
    protected URI hubLocation;

    public URI getHubLocation() {
        return hubLocation;
    }
}
