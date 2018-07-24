package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.net.URI;
import java.util.Arrays;

public enum HubEnvironment {
    PRODUCTION(
        URI.create("https://www.signin.service.gov.uk/SAML2/SSO"),
        URI.create("https://www.signin.service.gov.uk/SAML2/metadata/federation")
    ),
    INTEGRATION(
        URI.create("https://www.integration.signin.service.gov.uk/SAML2/SSO"),
        URI.create("https://www.integration.signin.service.gov.uk/SAML2/metadata/federation")
    ),
    COMPLIANCE_TOOL(
            URI.create("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/SSO"),
            URI.create("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation")
    ),
    COMPLIANCE_TOOL_LOCAL(
            URI.create("http://localhost:50270/SAML2/SSO"),
            URI.create("http://localhost:55000/compliance-tool-local/metadata.xml")
    );

    private URI ssoLocation;
    private URI metadataUri;

    @JsonCreator
    public static HubEnvironment fromString(String name) {
        return Arrays.stream(values())
            .filter(x -> name.equals(x.name()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Unrecognised Hub Environment: " + name + ". \n" +
                "Valid values are: PRODUCTION, INTEGRATION, COMPLIANCE_TOOL"
            ));
    }

    HubEnvironment(URI ssoLocation, URI metadataUri) {
        this.ssoLocation = ssoLocation;
        this.metadataUri = metadataUri;
    }

    public URI getSsoLocation() {
        return this.ssoLocation;
    }

    public URI getMetadataUri() {
        return this.metadataUri;
    }
}
