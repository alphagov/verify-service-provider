package uk.gov.ida.verifyserviceprovider;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class VerifyServiceProviderConfiguration extends Configuration {

    @JsonProperty
    @NotNull
    @Valid
    protected String helloWorldValue;

    public String getHelloWorldValue() {
        return helloWorldValue;
    }
}
