package uk.gov.ida.verifyserviceprovider.configuration;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public enum MetadataUri {

    PRODUCTION("Production", URI.create("https://www.signin.service.gov.uk/SAML2/metadata/federation")),
    INTEGRATION("Integration", URI.create("https://www.integration.signin.service.gov.uk/SAML2/metadata/federation")),
    COMPLIANCE_TOOL("Compliance Tool", URI.create("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation"));

    private String environment;
    private URI uri;

    MetadataUri(String environment, URI uri) {
        this.environment = environment;
        this.uri = uri;
    }

    public URI getUri(){
        return this.uri;
    }

    public static MetadataUri fromUri(URI uri) {
        return Arrays.stream(values())
            .filter(x -> Objects.equals(x.uri, uri))
            .findFirst()
            .orElseThrow(() -> getUnknownMetadataUriException(uri));
    }

    private static RuntimeException getUnknownMetadataUriException(URI uri) {
        return new RuntimeException("Unknown metadata uri: " + uri + ". Valid values are: " + "\n" + getDescription());
    }

    private static String getDescription(){
        return Arrays.stream(values())
            .map( item -> item.environment + ": " + item.uri)
            .collect(Collectors.joining("\n"));
    }
}
