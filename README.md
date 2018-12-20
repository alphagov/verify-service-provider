# Verify Service Provider

[![Build Status](https://travis-ci.org/alphagov/verify-service-provider.svg?branch=master)](https://travis-ci.org/alphagov/verify-service-provider)

GOV.UK Verify uses SAML (Security Assertion Markup Language) to securely exchange information about identities. A Relying Party can use Verify Service Provider to generate and translate SAML communication to and from the Verify Hub.

Using Verify Service Provider will make it easier to:
* connect multiple services with GOV.UK Verify - you only need one instance of Verify Service Provider
* handle certificate rotations - you can host multiple certificates at a time

You will need to host Verify Service Provider on your own infrastructure.

Using Verify Service Provider is just one part of connecting to GOV.UK Verify. Refer to the [technical onboarding guide](https://www.docs.verify.service.gov.uk) for more information about connecting to GOV.UK Verify.

See also:
* [API reference](/docs/api/api-reference.md)
* [How to use the VSP with your service](https://www.docs.verify.service.gov.uk/get-started-with-vsp/use-vsp-with-your-service)

## Setup

### Prerequisites

To download and use Verify Service Provider you must:
* have Java 8
* have a working [Matching Service Adapter](https://alphagov.github.io/rp-onboarding-tech-docs/pages/msa/msaUse.html)

### Download

[Download your own copy](https://github.com/alphagov/verify-service-provider/releases) of Verify Service Provider.

### Configure

Verify Service Provider comes with a default [YAML configuration file](https://github.com/alphagov/verify-service-provider/blob/master/verify-service-provider.yml)
called `verify-service-provider.yml` which you can customise either by providing environment variables or by editing the file directly.

By default the following environment variables are supported:

```
VERIFY_ENVIRONMENT            # The environment of the Verify Hub to run against - PRODUCTION, INTEGRATION, or COMPLIANCE_TOOL

SERVICE_ENTITY_IDS            # A JSON string array containing the entity id of the service using Verify Service Provider, e.g. '["http://entity-id"]'
                              # If you have multiple services using a single Verify Service Provider you should provide all of their entity IDs in this array.
MSA_ENTITY_ID                 # The SAML Entity Id that identifies the Relying Party's Matching Service Adapter
MSA_METADATA_URL              # The URL to the Matching Service Adapter's SAML metadata.

SAML_SIGNING_KEY              # A base64 encoded RSA private key that is used for signing the request to Verify
SAML_PRIMARY_ENCRYPTION_KEY   # A primary base64 encoded PKCS8 RSA private key that is used to decrypt encrypted SAML Assertions (see "Generating keys for testing")
SAML_SECONDARY_ENCRYPTION_KEY # (Optional - default empty) A secondary base64 encoded PKCS8 RSA private key that is used to decrypt encrypted SAML Assertions that
                              # will be used during certificate rotation events (see "Generating keys for testing")

PORT                          # (Optional - default 50400) The TCP port where the application will listen for HTTP traffic
LOG_LEVEL                     # (Optional - default INFO) The threshold level for logs to be written (e.g. DEBUG, INFO, WARN, or ERROR)
```

As Verify Service Provider is a Dropwizard application, you can also configure it with all [options provided by Dropwizard](http://www.dropwizard.io/1.3.5/docs/manual/configuration.html).

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
./bin/verify-service-provider server verify-service-provider.yml
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
You will need to either have environment variables set as above, or have edited the main configuration file (`verify-service-provider.yml`), or to pass an argument to this script for the application to start. Available arguments are `local-fed` for running against a locally running federation (see [verify local startup](https://github.com/alphagov/verify-local-startup)) or `vsp-only` for using default values to run against compliance tool on the reference environment.

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
