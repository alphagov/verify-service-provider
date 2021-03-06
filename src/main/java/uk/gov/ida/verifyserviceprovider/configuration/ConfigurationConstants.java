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
    String COMPLIANCE_HOST = "https://compliance-tool-integration.cloudapps.digital";

    String SSO_PATH = "/SAML2/SSO";
    String METADATA_PATH = "/SAML2/metadata/federation";

    String PRODUCTION_SSO = PRODUCTION_HOST + SSO_PATH;
    String PRODUCTION_METADATA = PRODUCTION_HOST + METADATA_PATH;

    String INTEGRATION_SSO = INTEGRATION_HOST + SSO_PATH;
    String INTEGRATION_METADATA = INTEGRATION_HOST +  METADATA_PATH;

    String COMPLIANCE_SSO = COMPLIANCE_HOST + SSO_PATH;
    String COMPLIANCE_METADATA = COMPLIANCE_HOST +  METADATA_PATH;

}
