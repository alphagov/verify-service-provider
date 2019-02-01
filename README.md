# Verify Service Provider

[![Build Status](https://travis-ci.org/alphagov/verify-service-provider.svg?branch=master)](https://travis-ci.org/alphagov/verify-service-provider)

GOV.UK Verify uses SAML (Security Assertion Markup Language) to securely exchange information about identities. A Relying Party can use Verify Service Provider to generate and translate SAML communication to and from the Verify Hub.

Using Verify Service Provider will make it easier to:
* connect multiple services with GOV.UK Verify - you only need one instance of Verify Service Provider
* handle certificate rotations - you can host multiple certificates at a time

You will need to host Verify Service Provider on your own infrastructure.

Using Verify Service Provider is the main part of connecting to GOV.UK Verify. Refer to the [technical documentation](https://alphagov.github.io/rp-onboarding-tech-docs/) for more information about using the Verify Service Provider and connecting to GOV.UK Verify.

See also:
* [API reference](/docs/api/api-reference.md)
* [Using the VSP](LINK)

## Setup

### Prerequisites

To use the Verify Service Provider, we recommend you use Java 11 or a long-term supported version of Java 8.

### Download

[Download your own copy](https://github.com/alphagov/verify-service-provider/releases) of the Verify Service Provider.

## Get started

GOV.UK Verify provides prebuilt clients for the following languages and frameworks:

|             Language / Framework               |                            Client Library                      |
|------------------------------------------------|----------------------------------------------------------------|
| node js / [passport.js](http://passportjs.org) | [passport-verify](https://github.com/alphagov/passport-verify) |

See [the API reference][vsp-api] for full details of the API.

## Run

The Verify Service Provider has several modes you can use:

Explain reasoning behind modes.

### Development mode

You can use development mode if you're building your own client for the VSP. For more information on building your own client using the VSP in development mode, see the [technical documentation on how to get started][vsp-get-started].

Development mode starts the VSP connected to a test tool hosted by the GOV.UK Verify team. The test tool acts as a placeholder for the GOV.UK Verify Hub. This means you can use your local setup to test if your service can respond appropriately to all possible scenarios in a Verify journey.

Every time you start the VSP in development mode, it initialises the test tool by:

- generating its self-signed keys and certificates
- adding the keys and certificates to the VSP configuration
- setting the test tool environment in the VSP configuration
- initialising an instance of the test tool

To start the VSP in development mode, run:

```
./bin/verify-service-provider development
```

You can use the following command line options to customise the behaviour of the VSP in development mode:

| Option | Description | Default |
| ------ | ----------- | ------- |
| `-d MATCHINGDATASET` or <br> `--matchingDataset MATCHINGDATASET`| The matching dataset the test tool will use | See technical documentation. |
|`-u URL` or<br> `--url URL` | The URL where the test tool will send responses | `http://localhost:8080/SAML2/Response` |
|`-t TIMEOUT` or<br> `--timeout TIMEOUT` | The timeout in seconds when communicating with the test tool | `5` |
| `-p PORT` or<br> `--port PORT` | The port the service will use | `50300` |
|`--host BINDHOST` | The host the service will bind to | `0.0.0.0` |


### Check mode

Checks if your configuration file is correct

### Server mode

Use this when you're deploying your VSP to the Integration or Production environments.

To run the application, export your environment variables and start the application with:

```
./bin/verify-service-provider server verify-service-provider.yml
```

The application will write logs to STDOUT.

You can check the application is running by calling the healthcheck endpoint with:

```
curl localhost:{$PORT}/admin/healthcheck
```

## Configure

Verify Service Provider comes with a default [YAML configuration file](https://github.com/alphagov/verify-service-provider/blob/master/verify-service-provider.yml)
called `verify-service-provider.yml` which you can customise either by providing environment variables or by editing the file directly.

By default the following environment variables are supported:

| Variable | Description |
| -------- | ----------- |
| `VERIFY_ENVIRONMENT` | The GOV.UK Verify Hub environment to run in.<br/>For example `PRODUCTION`, `INTEGRATION`|
| `SERVICE_ENTITY_IDS` | A JSON string array with the service's entity ID, for example `'["http://entity-id"]'`. If you have several services using one VSP deployment,<br/> the array should contain all of their service entity IDs. |
| `SAML_SIGNING_KEY` | A base64 encoded RSA private key used for signing the request to GOV.UK Verify Hub.|
| `SAML_PRIMARY_ENCRYPTION_KEY`| A primary base64 encoded PKCS8 RSA private key used to decrypt SAML responses.|
|`SAML_SECONDARY_ENCRYPTION_KEY`|(Optional - default empty) A secondary base64 encoded PKCS8 RSA private key is used to decrypt SAML responses. This parameter applies during [key rotation][key-rotation] events.|
| `PORT` | (Optional - default `50400`) The TCP port where the application will listen for HTTP traffic|
| `LOG_LEVEL` | Optional - default `INFO`) The threshold level for logs to be written, for example `DEBUG`, `INFO`, `WARN`, or `ERROR`) |

If you are using the legacy version involving a [Matching Service Adapter (MSA)](https://github.com/alphagov/verify-matching-service-adapter), two additional environment variables apply:

| Variable         | Description                         |
| ---------------- | ----------------------------------- |
| MSA_ENTITY_ID    | The `entityId` of the service's MSA |
| MSA_METADATA_URL | The URL to the MSA's metadata       |


Verify Service Provider is a Dropwizard application, so you can also configure it with the [options provided by Dropwizard](https://www.dropwizard.io).

## Contribute to the Verify Service Provider

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
[MIT License][mit-license]

[key-rotation]: https://www.docs.verify.service.gov.uk/maintain-your-connection/rotate-keys/
[vsp-get-started]: https://www.docs.verify.service.gov.uk/set-up-vsp-with-your-service/get-started-with-the-vsp
[vsp-api]: https://github.com/alphagov/verify-service-provider/blob/master/architecture-decisions/verify-service-provider-api.swagger.yml
[mit-license]: https://github.com/alphagov/verify-service-provider/blob/master/LICENSE
