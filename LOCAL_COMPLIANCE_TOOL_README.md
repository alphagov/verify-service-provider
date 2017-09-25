## Run the Verify Service Provider against a local compliance tool

To run VSP against a locally running compliance tool you need to make a number of changes.

### ida-compliance-tool:
-> Add `hubExpectedToSignAuthnResponse: true` to configuration/local/compliance-tool.yml
(Currently sample-rp doesn't do this check but the VSP does so we need this signature in the VSP)

### verify-metadata:
-> Comment out `:SSLEnable => true` and `:SSLCertName => cert_name` in tools/local_metadata_server
(It currently self-signs it's TLS cert and the VSP won't trust it - we might be able to fiddle this)

### ida-hub-acceptance-tests:
-> Change line 20 of compliance-tool-startup from `https` -> `http` in the second argument to second
(Disabling self-signing means that metadata now runs on http not https)

### verify-service-provider:
-> Change hubSsoLocation in configuration/verify-service-provider.yml to `http://localhost:50270/SAML2/SSO`

-> Copy `verify-test-truststore.ts` from src/main/resources to the verify-service-provider root directory

-> Change verifyHubMetadata section to
```
verifyHubMetadata:
  uri: ${HUB_METADATA_URL:-http://localhost:55000/compliance-tool-local/metadata.xml}
  trustStorePath: verify-test-truststore.ts
```
The above will allow you to run the VSP against compliance tool locally if you configure (with POST request) it properly.

### To run service provider acceptance-tests:
-> Change `COMPLIANCE_TOOL_HOST` in AuthnRequestAcceptanceTest to `http://localhost:50270`

-> Change the verifyHubMetadata.uri ConfigOverride in VerifyServiceProviderAppRule to `http://localhost:55000/compliance-tool-local/metadata.xml`

-> Change `HOST` in ComplianceToolService to `http://localhost:50270`

-> Either comment out the ```expectedEntityId``` assertion in ApplicationConfigurationFeatureTests or change it to use `https://local.sigin.service.gov.uk`