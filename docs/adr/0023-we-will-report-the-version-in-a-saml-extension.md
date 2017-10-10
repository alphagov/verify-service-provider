# 23. We will report the version in a saml extension

Date: 2017-10-10

## Status

Accepted

## Context

The verify-service-provider will be deployed to a number of relying parties.
Which relying party is using which version could potentially become difficult
to keep track of.

To make it as easy as possible for us to manage this we'd like the
verify-service-provider to report its version in some way.

Because the verify-service-provider is not intended to be accessible to the
internet we can't simply expose an endpoint that reports the version number.
Also, since the SAML messages go via the browser we can't use a custom HTTP
header.

There's also a concern about the security implications of reporting a version
number in cleartext.

We considered a couple of options:

- Requesting metadata from Verify with a custom user-agent string
- Sending the version in an unencrypted saml extension
- Sending the version in an encrypted saml extension

## Decision

We decided to send the version number in the SAML AuthnRequests as an encrypted
SAML extension. The XML will look roughly like this:

```
<saml:AuthnRequest>
  <saml:Issuer>...</saml:Issuer>
  <saml:Signature>...</saml:Signature>
  <saml:Extensions>
    <saml:EncryptedAttribute>...</saml:EncryptedAttribute>
  </saml:Extensions>
</saml:AuthnRequest>
```

Once decrypted, the Attribute in the Extensions will look like:

```
<saml:Attribute Name="Versions">
  <saml:AttributeValue xsi:type="metrics:VersionsType">
    <metrics:ApplicationVersion>3.4.1</metrics:ApplicationVersion>
  </saml:AttributeValue>
</saml:Attribute>
```

## Consequences

Verify will be able to monitor the versions of connected instances of the
verify-service-provider.

AuthnRequests sent by the verify-service-provider will be approximately 2,500
bytes longer than they would be without the version extension. They should
still be short enough that this will not cause any validation issues.

Version numbers will not be visible to malicious third parties as the extension
will be encrypted with Verify's public key.

