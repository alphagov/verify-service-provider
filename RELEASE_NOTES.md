Release notes
=============

### Next

* Change trust store configuration schema to match MSA

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

