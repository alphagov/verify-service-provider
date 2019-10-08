package uk.gov.ida.verifyserviceprovider.configuration;

public interface ConfigurationConstants {
    /**
     * Note: our trust stores do not contain private keys,
     * so this password does not need to be managed securely.
     *
     * This password MUST NOT be used for anything sensitive, since it is open source.
     */
    String DEFAULT_TRUST_STORE_PASSWORD = "bj76LWZ+F5L1Biq4EZB+Ta7MUY4EQMgmZmqAHh";

    String PRODUCTION_METADATA_TRUSTSTORE = "prod-metadata-truststore.ts";
    String TEST_METADATA_TRUSTSTORE = "test-metadata-truststore.ts";
    String PRODUCTION_HUB_TRUSTSTORE = "prod-hub-truststore.ts";
    String TEST_HUB_TRUSTSTORE = "test-hub-truststore.ts";
    String PRODUCTION_IDP_TRUSTSTORE = "prod-idp-truststore.ts";
    String TEST_IDP_TRUSTSTORE = "test-idp-truststore.ts";

    String HUB_JERSEY_CLIENT_NAME = "VerifyHubMetadataClient";
    String MSA_JERSEY_CLIENT_NAME = "MsaMetadataClient";

    String PRODUCTION_HOST ="https://www.signin.service.gov.uk";
    String INTEGRATION_HOST ="https://www.integration.signin.service.gov.uk";
    String COMPLIANCE_HOST = "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk";
    String LOCAL_HOST = "http://localhost";

    String SSO_PATH = "/SAML2/SSO";
    String METADATA_PATH = "/SAML2/metadata/federation";
    String TRUSTANCHOR_PATH = "/SAML2/metadata/trust-anchor";
    String METADATASOURCE_PATH = "/SAML2/metadata/aggregator";

    String PRODUCTION_SSO = PRODUCTION_HOST + SSO_PATH;
    String PRODUCTION_METADATA = PRODUCTION_HOST + METADATA_PATH;
    String PRODUCTION_TRUSTANCHOR_URI = PRODUCTION_HOST + TRUSTANCHOR_PATH;
    String PRODUCTION_METADATASOURCE_URI = PRODUCTION_HOST + METADATASOURCE_PATH;

    String[] PRODUCTION_ACCEPTABLE_HUBCONNECTOR_ENTITY_IDS = new String[] {
        "https://www.signin.service.gov.uk/SAML2/metadata/connector",               // (AWS)
        "https://connector-node.london.verify.govsvc.uk/ConnectorMetadata",         // (GSP)
        "https://eidas.signin.service.gov.uk/ConnectorMetadata",                    // (potential new #1)
        "https://connector.eidas.signin.service.gov.uk/ConnectorMetadata",          // (potential new #2)
        "https://connector-node.eidas.signin.service.gov.uk/ConnectorMetadata",     // (potential new #3)
    };

    String INTEGRATION_SSO = INTEGRATION_HOST + SSO_PATH;
    String INTEGRATION_METADATA = INTEGRATION_HOST +  METADATA_PATH;
    String INTEGRATION_TRUSTANCHOR_URI = INTEGRATION_HOST + TRUSTANCHOR_PATH;
    String INTEGRATION_METADATASOURCE_URI = INTEGRATION_HOST + METADATASOURCE_PATH;

    String[] INTEGRATION_ACCEPTABLE_HUBCONNECTOR_ENTITY_IDS = new String[] {
        "https://www.integration.signin.service.gov.uk/SAML2/metadata/connector",               // (AWS)
        "https://connector-node-integration.london.verify.govsvc.uk/ConnectorMetadata",         // (GSP)
        "https://eidas.integration.signin.service.gov.uk/ConnectorMetadata",                    // (potential new #1)
        "https://connector.eidas.integration.signin.service.gov.uk/ConnectorMetadata",          // (potential new #2)
        "https://connector-node.eidas.integration.signin.service.gov.uk/ConnectorMetadata",     // (potential new #3)
    };

    String COMPLIANCE_SSO = COMPLIANCE_HOST + SSO_PATH;
    String COMPLIANCE_METADATA = COMPLIANCE_HOST +  METADATA_PATH;
    String COMPLIANCE_TRUSTANCHOR_URI = COMPLIANCE_HOST +  TRUSTANCHOR_PATH;
    String COMPLIANCE_METADATASOURCE_URI = COMPLIANCE_HOST + METADATASOURCE_PATH;

    String[] COMPLIANCE_ACCEPTABLE_HUBCONNECTOR_ENTITY_IDS = new String[] {
        "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/connector",         // (AWS)
        "connector-node-compliance-tool-reference.london.verify.govsvc.uk/ConnectorMetadata",                   // (GSP)
        "https://eidas.compliance-tool-reference.signin.service.gov.uk/ConnectorMetadata",                      // (potential new #1)
        "https://connector.eidas.compliance-tool-reference.signin.service.gov.uk/ConnectorMetadata",            // (potential new #2)
        "https://compliance-tool-reference-node.eidas.integration.signin.service.gov.uk/ConnectorMetadata",     // (potential new #3)
    };

    String LOCAL_SSO = LOCAL_HOST + ":55000/SAML2/SSO";
    String LOCAL_METADATA = LOCAL_HOST +  ":55000/local/metadata.xml";
    String LOCAL_TRUSTANCHOR_URI = LOCAL_HOST + ":55000/local/trust-anchor.jws";
    String LOCAL_METADATASOURCE_URI = LOCAL_HOST + ":55001";
    String LOCAL_HUBCONNECTOR_ENTITY_ID = LOCAL_HOST + ":55000/local-connector/metadata.xml";

    String[] LOCAL_ACCEPTABLE_HUBCONNECTOR_ENTITY_IDS = new String[] {
            LOCAL_HUBCONNECTOR_ENTITY_ID
    };


}
