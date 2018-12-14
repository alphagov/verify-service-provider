package uk.gov.ida.verifyserviceprovider.builders;

import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;

public class VerifyServiceProviderAppRuleBuilder {

    private MockMsaServer mockMsaServer;

    public static VerifyServiceProviderAppRuleBuilder aVerifyServiceProviderAppRule() {
        return new VerifyServiceProviderAppRuleBuilder();
    }

    public VerifyServiceProviderAppRule build() {
        return new VerifyServiceProviderAppRule();
    }

    public VerifyServiceProviderAppRuleBuilder withMockMsaServer(MockMsaServer mockMsaServer) {
        this.mockMsaServer = mockMsaServer;
        return this;
    }
}
