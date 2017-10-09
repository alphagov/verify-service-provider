## Advanced Configuration for the Verify Service Provider

During development, you may find you need to make use of advanced configuration options, which are not expected to
be used by relying parties in general. Available options are documented here.

### Hub Configuration

You can override the hubSsoLocation and the hub metadata configuration in a configuration yaml file by adding to the
verifyHubConfiguration section as follows:
```
verifyHubConfiguration:
  environment: ... # Required.
  hubSsoLocation: ... # The location where Verify Hub authentication flow begins, e.g. https://www.integration.signin.service.gov.uk/SAML2/SSO
  metadata:
    uri: ... # The location of hub metadata, e.g. https://www.integration.signin.service.gov.uk/SAML2/metadata/federation
    expectedEntityId: ... # i.e. of hub (e.g. https://signin.service.gov.uk)
    trustStorePath: ...
    trustStorePassword: ...
    minRefreshDelay: ...
    maxRefreshDelay: ...
    jerseyClientConfiguration: ...
    jerseyClientName: ...
```

`environment` is, as always, required to be specified, and must be one of PRODUCTION, INTEGRATION, or COMPLIANCE_TOOL.
Other values are all optional overrides, and will default to values based on the chosen environment. Values need only
be specified for these if you do not wish to use the default value for the chosen environment.

### MSA Metadata Configuration

You can override some properties of the MSA metadata configuration in a configuration yaml file by adding to the
msaMetadata section as follows:
```
msaMetadata:
  uri: ... # Required
  expectedEntityId: ... # Required
  minRefreshDelay: ...
  maxRefreshDelay: ...
  jerseyClientConfiguration: ...
  jerseyClientName: ...
```

Note there are no trust stores for the MSA metadata, since it is not signed. Default values will be used for any
of the above non-required options which do not have values specified.

