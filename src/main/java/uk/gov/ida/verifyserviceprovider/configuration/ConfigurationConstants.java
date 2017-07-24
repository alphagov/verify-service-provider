package uk.gov.ida.verifyserviceprovider.configuration;

public interface ConfigurationConstants {
    String PRODUCTION_METADATA_URI = "https://www.signin.service.gov.uk/SAML2/metadata/federation";
    String INTEGRATION_METADATA_URI = "https://www.integration.signin.service.gov.uk/SAML2/metadata/federation";
    String COMPLIANCE_TOOL_METADATA_URI = "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation";

    String PRODUCTION_VERIFY_TRUSTSTORE_NAME = "verify-production-truststore.ts";
    String TEST_VERIFY_TRUSTSTORE_NAME = "verify-test-truststore.ts";

    String HUB_JERSEY_CLIENT_NAME = "VerifyHubMetadataClient";
    String MSA_JERSEY_CLIENT_NAME = "MsaMetadataClient";
}
