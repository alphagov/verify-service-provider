Release notes
=============

### Next

#### Connect to Verify using only the VSP

This release adds the ability to connect to GOV.UK Verify using only the Verify Service Provider (VSP). This means services can connect without needing to host a [Matching Service Adapter (MSA)](https://github.com/alphagov/verify-matching-service-adapter). This makes it easier to connect to Verify and reduces maintenance tasks once connected.

When used alone, the VSP provides MSA functionality. It can:
* implement security features to the same security level as the MSA
* handle European identities
* do Personal Identifier (PID) hashing

If you are already connected to GOV.UK Verify and are running an MSA, you can upgrade to VSP 2.0.0 without needing to do any configuration changes.

If you want to switch off your Matching Service Adapter and only use the Verify Service Provider to connect to Verify, [contact the GOV.UK Verify Team](https://www.verify.service.gov.uk/support/).

For more information on connecting to GOV.UK Verify using only the VSP, see the [technical documentation](https://www.docs.verify.service.gov.uk).

#### Tutorial for the new VSP

We moved the [VSP tutorial](https://www.docs.verify.service.gov.uk/get-started-with-vsp/#get-started-with-the-verify-service-provider) to the new technical documentation website and updated it to help you connect to GOV.UK verify using the VSP.

#### Support for compilation on Java 9 and 10

Compilation on Java 9 or 10 will produce a multi-release JAR that will run on Java 8, 9, and 10. This release was compiled on Java 8 and does not contain a multi-release JAR.

#### Metadata health check changes

Changed **HOW?** metadata health check names to use the URI of the metadata they are trying to resolve


### 1.0.0
[View Diff](https://github.com/alphagov/verify-service-provider/compare/0.4.0...1.0.0)

* Change trust store configuration schema to match MSA
* Make the banner that prints on startup less wide

#### Configuration Changes:
* If using a custom file-based trust store configuration, replace:
```diff
- trustStorePath: /path/to/file
- trustStorePassword: foobar

+ trustStore:
+   path: /path/to/file
+   password: foobar
```
### 0.4.0

* Add support for the Address History user account creation attribute
* Add ENVIRONMENT configuration option to replace hubSsoLocation and metadataUrl.
* Send version number to hub
* Support multitenancy
* Improve documentation
* Improve healthcheck logging

#### Configuration Changes:
When using environment variables:
* Replace SERVICE_ENTITY_ID with SERVICE_ENTITY_IDS, which is a JSON string array containing the entity id of the service (or services) using the Verify Service Provider (e.g. '["http://entity-id"]')
* Remove HUB_METADATA_URL and HUB_SSO_LOCATION
* Add VERIFY_ENVIRONMENT, specifying the environment of the Verify Hub to run against. Valid values are PRODUCTION, INTEGRATION, or COMPLIANCE_TOOL.

When using a yaml file:
* Replace serviceEntityId with serviceEntityIds, which is a list containing the entity id (or ids) as above
* Remove hubSsoLocation and verifyHubMetadata
* Add verifyHubConfiguration as below. This will contain an environment option specifying which hub environment to use, removing the need to specify the hubSsoLocation or metadata url.
```
verifyHubConfiguration:
  environment: \\ PRODUCTION, INTEGRATION or COMPLIANCE_TOOL
```

### 0.3.0

* Fix the expected names of user account creation attributes to match the names the MSA produces
* Fix the expected format of the date of birth attribute value to match the format the MSA produces

### 0.2.0

Initial release candidate of verify service provider. Now contains a fully
functional SAML service provider implementation that works with GOV.UK Verify.

### 0.1.0-prototype-0

Pre-release of verify service provider prototype-0.
