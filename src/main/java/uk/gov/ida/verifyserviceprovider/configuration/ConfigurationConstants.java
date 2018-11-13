package uk.gov.ida.verifyserviceprovider.configuration;

public interface ConfigurationConstants {
    /**
     * Note: our trust stores do not contain private keys,
     * so this password does not need to be managed securely.
     *
     * This password MUST NOT be used for anything sensitive, since it is open source.
     */
    String DEFAULT_TRUST_STORE_PASSWORD = "bj76LWZ+F5L1Biq4EZB+Ta7MUY4EQMgmZmqAHh";

    String PRODUCTION_VERIFY_TRUSTSTORE_NAME = "verify-production-truststore.ts";
    String TEST_VERIFY_TRUSTSTORE_NAME = "verify-test-truststore.ts";

    String HUB_JERSEY_CLIENT_NAME = "VerifyHubMetadataClient";
    String MSA_JERSEY_CLIENT_NAME = "MsaMetadataClient";
}
