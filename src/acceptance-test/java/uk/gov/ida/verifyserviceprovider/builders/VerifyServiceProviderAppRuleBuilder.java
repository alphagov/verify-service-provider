package uk.gov.ida.verifyserviceprovider.builders;

import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import uk.gov.ida.verifyserviceprovider.rules.V1VerifyServiceProviderAppRule;

public class VerifyServiceProviderAppRuleBuilder {

    private MockMsaServer mockMsaServer;

    public static VerifyServiceProviderAppRuleBuilder aVerifyServiceProviderAppRule() {
        return new VerifyServiceProviderAppRuleBuilder();
    }

    public V1VerifyServiceProviderAppRule build() {
        return new V1VerifyServiceProviderAppRule(mockMsaServer);
    }

    public VerifyServiceProviderAppRuleBuilder withMockMsaServer(MockMsaServer mockMsaServer) {
        this.mockMsaServer = mockMsaServer;
        return this;
    }
}
