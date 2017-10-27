# Verify Service Provider

GOV.UK Verify uses SAML (Security Assertion Markup Language) to securely exchange information about identities. A Relying Party can use Verify Service Provider to handle all SAML communication to and from the Verify Hub.

Using Verify Service Provider will make it easier to:
* connect multiple services with GOV.UK Verify - you only need one instance of Verify Service Provider
* handle certificate rotations - you can host multiple certificates at a time

You will need to host Verify Service Provider on your own infrastructure.

Using Verify Service Provider is just one part of connecting to GOV.UK Verify. Refer to the [technical onboarding guide](https://alphagov.github.io/rp-onboarding-tech-docs/) for more information about connecting to GOV.UK Verify.

## Setup

### Download

Verify Service Provider is not available over the internet. Contact [idasupport+onboarding@digital.cabinet-office.gov.uk](mailto:idasupport+onboarding@digital.cabinet-office.gov.uk) for a release version.

### Configure

To configure Verify Service Provider you can either:
* amend the [YAML configuration file](https://github.com/alphagov/verify-service-provider/blob/master/configuration/verify-service-provider.yml)
* define environment variables

To define environment variables use:

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

As Verify Service Provider is a Dropwizard application, you can also configure it with all [options provided by Dropwizard](http://www.dropwizard.io/1.1.0/docs/manual/configuration.html).

### Supporting multiple services connected to GOV.UK Verify

You can use a single instance of Verify Service Provider with multiple different services. To do this, you must include a list of all possible service entity IDs in your configuration.

There are 2 ways to include service entity IDs:
* amend the [YAML configuration file](https://github.com/alphagov/verify-service-provider/blob/master/configuration/verify-service-provider.yml)
* add more than one item in the JSON array for the `SERVICE_ENTITY_IDS` environment variable

### Generate keys for testing

In order to generate keys for testing, we recommend using [OpenSSL](https://www.openssl.org).

You can generate a private key by:
1. generating an RSA key in PEM format
2. converting the key to base64 encoded PKCS8

Generate an RSA key in PEM format with:
```
openssl genrsa -des3 -passout pass:x 2048 | openssl rsa -passin pass:x -out key-name.pem
```

Convert the PEM formatted key to base64 encoded PKCS8 for the config file. Print the key to STDOUT with:
```
openssl pkcs8 -topk8 -inform PEM -outform DER -in key-name.pem -nocrypt | openssl base64 -A; echo
```

### Run

To run the application, export your environment variables and start the application with:

```
./bin/verify-service-provider
```

The application will write logs to STDOUT.

You can check the application is running by calling the healthcheck endpoint with:

```
curl localhost:{$PORT}/admin/healthcheck
```

## Usage

GOV.UK Verify provides prebuilt clients for the following languages and frameworks:

|             Language / Framework               |                            Client Library                      |
|------------------------------------------------|----------------------------------------------------------------|
| node js / [passport.js](http://passportjs.org) | [passport-verify](https://github.com/alphagov/passport-verify) |

See [the API reference](https://github.com/alphagov/verify-service-provider/blob/master/architecture-decisions/verify-service-provider-api.swagger.yml) for full details of the API.

## Development

If you want to make changes to `verify-service-provider` itself, fork the repository then:

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

You can find the distribution zip at `build/distributions`.

See [docs/development](https://github.com/alphagov/verify-service-provider/tree/master/docs/development) for more information about the development of Verify Service Provider, including how to run the application against a local compliance tool and see advanced configuration options.

## Support and raising issues

If you think you have discovered a security issue in this code please email [disclosure@digital.cabinet-office.gov.uk](mailto:disclosure@digital.cabinet-office.gov.uk) with details.

For non-security related bugs and feature requests please [raise an issue](https://github.com/alphagov/verify-service-provider/issues/new) in the GitHub issue tracker.

## Licensing
[MIT License](https://github.com/alphagov/verify-service-provider/blob/master/LICENSE)
