Verify Service Provider
=======================

Verify Service Provider is a Service that provides support for the communication
between Verify Hub and a Relying Party.

Please refer to the [techinical onboarding guide](https://alphagov.github.io/rp-onboarding-tech-docs/) to understand how services can technically integrate with Verify.

Setup
-----

### Download

Release versions of the Verify Service Provider are available upon request from idasupport+onboarding@digital.cabinet-office.gov.uk

### Configure

The easiest way to configure the application is to use environment variables.
See [Advanced Configuration](#advanced-configuration) for more options.

The following Environment Variables can be defined:

```
#!/usr/bin/env sh

export SERVICE_ENTITY_IDS=... # A JSON string array containing the entity id of the service using Verify Service Provider, e.g. '["http://entity-id"]'.
export PORT=... # The TCP port where the application will listen for HTTP traffic
export LOG_LEVEL=... # The threshold level for logs to be written (e.g. DEBUG, INFO, WARN, or ERROR) (default: INFO)
export MSA_ENTITY_ID=... # The SAML Entity Id that identifies the Relying Party's Matching Service Adapter
export MSA_METADATA_URL=... # The URL to the Matching Service Adapter's SAML metadata.
export VERIFY_ENVIRONMENT=... # The environment of the Verify Hub to run against - PRODUCTION, INTEGRATION, or COMPLIANCE_TOOL
export SAML_SIGNING_KEY=... # A base64 encoded RSA private key that is used for signing the request to Verify
export SAML_PRIMARY_ENCRYPTION_KEY=... # A primary base64 encoded PKCS8 RSA private key that is used to decrypt encrypted SAML Assertions (see "Generating keys for testing")
export SAML_SECONDARY_ENCRYPTION_KEY=... # (Optional) A secondary base64 encoded PKCS8 RSA private key that is used to decrypt encrypted SAML Assertions that will be used during certificate rotation events (see "Generating keys for testing")

# Run the application with the above variables set
./bin/verify-service-provider
```

#### Generating keys for testing

If you have [openssl](https://www.openssl.org) installed you can generate a private key in the correct format with:

```
# Generate an RSA key in PEM format
openssl genrsa -des3 -passout pass:x 2048 | openssl rsa -passin pass:x -out key-name.pem

# Convert the PEM formatted key to base64 encoded PKCS8 for the config file. Print the key to standard out.
openssl pkcs8 -topk8 -inform PEM -outform DER -in key-name.pem -nocrypt | openssl base64 -A; echo
```

### Run

```
# Export your Environment Variables, then start the application with:
./bin/verify-service-provider
```

The application will write logs to STDOUT.

You can check that the application is running by calling the healthcheck path:
```
curl localhost:{$PORT}/admin/healthcheck
```


## Usage

There are prebuilt clients for the following languages and frameworks:

|             Language / Framework               |                            Client Library                      |
|------------------------------------------------|----------------------------------------------------------------|
| node js / [passport.js](http://passportjs.org) | [passport-verify](https://github.com/alphagov/passport-verify) |

See [the swagger documentation](
https://github.com/alphagov/verify-service-provider/blob/master/architecture-decisions/verify-service-provider-api.swagger.yml
) for details of the API.

## Advanced Configuration

Yaml based configuration provides more fine grained controls over the application. For example, you are able to configure HTTPS endpoints.

See the reference configuration file available at [verify-service-provider.yml](
https://github.com/alphagov/verify-service-provider/blob/master/configuration/verify-service-provider.yml
)

Verify Service Provider is a Dropwizard application and thus supports all configuration options
provided by Dropwizard: http://www.dropwizard.io/1.1.0/docs/manual/configuration.html

## Multi Tenancy

A single instance of the Verify Service Provider can be used with multiple different services.
To do so, a list of all the possible service entity ids must be included in the configuration.

This can be done by either in a yaml configuration file (see above) or by putting more than one
item in the JSON array for the SERVICE_ENTITY_IDS environment variable.

Development
-----------

If you want to make changes to the `verify-service-provider` itself, fork the repository then:

__Test__
```
./pre-commit.sh
```

__Startup__
```
./startup.sh
```

__Build a distribution__
```
./gradlew distZip
```

The distribution zip can be found at `build/distributions`.

See `docs/development` for useful information for development on the service provider, including
how to run against a local compliance tool and some advanced configuration options.

## Responsible Disclosure

If you think you have discovered a security issue in this code please email disclosure@digital.cabinet-office.gov.uk with details.

For non-security related bugs and feature requests please [raise an issue](https://github.com/alphagov/verify-service-provider/issues/new) in the github issue tracker.

