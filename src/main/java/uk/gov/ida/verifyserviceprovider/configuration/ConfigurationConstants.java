package uk.gov.ida.verifyserviceprovider.configuration;

public interface ConfigurationConstants {
    /**
     * Note: our trust stores do not contain private keys,
     * so this password does not need to be managed securely.
     *
     * This password MUST NOT be used for anything sensitive, since it is open source.
     */
    String DEFAULT_TRUST_STORE_PASSWORD = "bj76LWZ+F5L1Biq4EZB+Ta7MUY4EQMgmZmqAHh";

    String TEST_METADATA_TRUSTSTORE_NAME = "test-metadata-truststore.ts";
    String TEST_HUB_TRUSTSTORE_NAME = "test-hub-truststore.ts";
    String TEST_IDP_TRUSTSTORE_NAME = "test-idp-truststore.ts";

    String PROD_METADATA_TRUSTSTORE_NAME = "prod-metadata-truststore.ts";
    String PROD_HUB_TRUSTSTORE_NAME = "prod-hub-truststore.ts";
    String PROD_IDP_TRUSTSTORE_NAME = "prod-idp-truststore.ts";

    String HUB_JERSEY_CLIENT_NAME = "VerifyHubMetadataClient";
    String MSA_JERSEY_CLIENT_NAME = "MsaMetadataClient";

    interface EnvironmentVariables {
        String HUB_SSO_URL = "HUB_SSO_URL";
        String HUB_METADATA_URL = "HUB_METADATA_URL";
        String HUB_EXPECTED_ENTITY_ID = "HUB_EXPECTED_ENTITY_ID";
        String METADATA_TRUSTSTORE_PATH = "METADATA_TRUSTSTORE_PATH";
        String HUB_TRUSTSTORE_PATH = "HUB_TRUSTSTORE_PATH";
        String IDP_TRUSTSTORE_PATH = "IDP_TRUSTSTORE_PATH";
        String TRUSTSTORE_PASSWORD = "TRUSTSTORE_PASSWORD";
    }
}
