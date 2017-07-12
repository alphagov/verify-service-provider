Verify Service Provider
=======================

Verify Service Provider is a Service that provides support for the communication
between Verify Hub and a Relying Party.

Please refer to the [techinical onboarding guide](https://alphagov.github.io/rp-onboarding-tech-docs/) to understand how services can technically integrate with Verify.

__NOTE__: The Verify Service Provider is currently in a prototyping phase of development


Setup
-----

### Download

Release versions of the Verify Service Provider are available upon request from the Verify Support Team.

### Configure

The application accepts environment variables or a Dropwizard-style yaml configuration file for finer controls. 

It is recommended that you choose one mechanism or the other.

__NOTE__: These options may change during development

##### Environment variables

The following Environment Variables can be defined:

* `PORT` - The TCP port where the application will listen for HTTP traffic
* `LOG_LEVEL` - The threshold level for logs to be written (e.g. DEBUG, INFO, WARN, or ERROR) (default: INFO)
* `MSA_ENTITY_ID` - The SAML Entity Id that identifies the Relying Partie's Matching Service Adapter
* `MSA_METADATA_URL` - The URL to the Matching Service Adapter's SAML metadata.
* `HUB_ENTITY_ID` - The SAML Entity Id that identifies the Verify Hub (default: https://signin.service.gov.uk)
* `HUB_METADATA_URL` - The URL to the Verify Hub's SAML metadata. (default: https://www.signin.service.gov.uk/SAML2/metadata/federation
* `SAML_SIGNING_KEY` - A base64 encoded RSA private key that is used for signing the request to Verify
* `SAML_SIGNING_CERTIFICATE` - A base64 encoded x509 certificate that corresponds to the above key
* `SAML_PRIMARY_ENCRYPTION_KEY` - A primary base64 encoded RSA private key that is used to decrypt encrypted SAML Assertions
* `SAML_PRIMARY_ENCRYPTION_CERTIFICATE` - A base64 encoded x509 certificate key that corresponds to the above key
* `SAML_SECONDARY_ENCRYPTION_KEY` - (Optional) A secondary base64 encoded RSA private key that is used to decrypt encrypted SAML Assertions that will be used during certificate rotation events
* `SAML_SECONDARY_ENCRYPTION_CERTIFICATE` - (Optional) A base64 encoded x509 certificate key that corresponds to the above key

#### Yaml based configuration

Yaml based configuration provides more fine grained controls over the application. For example, you are able to configure HTTPS endpoints.

See the reference configuration file available at [configuration/verify-service-provider.yml](
https://github.com/alphagov/verify-service-provider/blob/master/prototypes/prototype-0/verify-service-provider/configuration/verify-service-provider.yml
)

Verify Service Provider is a Dropwizard application and thus supports all configuration options
provided by Dropwizard: http://www.dropwizard.io/1.1.0/docs/manual/configuration.html

### Run

Running the application varies depending on whether you wish to use Environment Variables or Yaml based configuration

For Environment Variables:

```
## Export Environment Variables
./bin/verify-service-provider
```

Or with a Yaml file:

```
./bin/verify-service-provider server config.yml
```

## Integrate

Verify Service Provider provides a HTTP API that supports the communication between the Relying party
and Verify. Please see our [swagger documentation](https://github.com/alphagov/verify-service-provider/blob/master/prototypes/prototype-0/docs/verify-service-provider-api.swagger.yml) for details of this.

Development
-----------

__Test__
```
./pre-commit.sh
```

__Startup__
```
./startup.sh
```

Remember to run acceptance tests after changes https://github.com/alphagov/verify-service-provider/tree/master/prototypes/prototype-0/acceptance-tests

