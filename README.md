[![Build Status](https://travis-ci.com/alphagov/verify-service-provider.svg?branch=master)](https://travis-ci.com/alphagov/verify-service-provider)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/accd9189851c4f1a9830105a0349c535)](https://www.codacy.com/app/alphagov/verify-service-provider?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=alphagov/verify-service-provider&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/accd9189851c4f1a9830105a0349c535)](https://www.codacy.com/app/alphagov/verify-service-provider?utm_source=github.com&utm_medium=referral&utm_content=alphagov/verify-service-provider&utm_campaign=Badge_Coverage)

# Verify Service Provider

The Verify Service Provider (VSP) generates and translates Security Assertion Markup Language (SAML) messages to and from the GOV.UK Verify Hub. SAML is an open standard for secure message exchange which GOV.UK Verify uses when handling information about identities.

Using the VSP removes the need for services to handle SAML by:

* generating SAML requests to send to the GOV.UK Verify Hub
* translating SAML responses from the GOV.UK Verify Hub into JSON

Services will need to host the VSP on their own infrastructure.

The VSP allows you to:

- handle signing and encryption key rotation without service downtime.
- connect multiple services to GOV.UK Verify using the same VSP deployment

See also:
* [Technical documentation for connecting services][tech-docs]
* [Setting up the VSP][vsp-get-started]
* [VSP API reference documentation][vsp-api-reference]
* [OpenAPI v3 VSP API specification][vsp-api-spec]


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

If you're building or setting up your own client for the VSP, see the [technical documentation on how to get started with the VSP][vsp-get-started].

See [the API reference][vsp-api-reference] for full details of the API.

## Run

You can run the the VSP using several commands, depending on your development needs:

| Command       | Use case                               |
| ------------- | ----------------------------------------- |
| `development` | Local development of a VSP client <br> The VSP is connected to a testing service hosted by the GOV.UK Verify team. |
| `server`      | End-to-end testing and running in production <br> The VSP uses the configuration provided to connect to a full-scale deployment of the GOV.UK Verify Hub.                                          |
| `check`       | Validates your configuration file.        |

### `development`

You can use the `development` command if you're building your own client for the VSP. The command starts the VSP connected to a testing service hosted by the GOV.UK Verify team. The testing service acts as a placeholder for the GOV.UK Verify Hub. This means you can use your local setup to test if your service can respond appropriately to all possible scenarios in a Verify journey.

When running the VSP using the `development` command, it initialises the testing service by:

- generating its self-signed keys and certificates
- adding the keys and certificates to the VSP configuration
- setting the testing service environment in the VSP configuration
- initialising a testing session with the testing service

To start the VSP connected to the testing service, run:

```
./bin/verify-service-provider development
```

You can use the following command line options to customise the behaviour of the `development` command:

| Option | Description | Default |
| ------ | ----------- | ------- |
| `-d IDENTITYDATASET` or <br> `--identityDataset IDENTITYDATASET`| The identity dataset the testing service will use | [Test identity dataset][identity-dataset] |
|`-u URL` or<br> `--url URL` | The URL where the testing service will send responses | `http://localhost:8080/SAML2/Response` |
|`-t TIMEOUT` or<br> `--timeout TIMEOUT` | The timeout in seconds when communicating with the testing service | `5` |
| `-p PORT` or<br> `--port PORT` | The port the service will use | `50300` |
|`--host BINDHOST` | The host the service will bind to | `0.0.0.0` |

You can check the application is running by calling the healthcheck endpoint with:

```
curl localhost:{$PORT}/admin/healthcheck
```

For more information on building your own client using the `development` command, see the [technical documentation on how to get started][vsp-get-started].

### `server`

Use the `server` command when running the VSP in an environment containing a full-scale deployment of the GOV.UK Verify Hub, for example the Integration or Production environments.

To run the VSP using the [environment and security configuration][configuration] in `verify-service-provider.yml`, export your environment variables and run:

```
./bin/verify-service-provider server verify-service-provider.yml
```

The application will write logs to STDOUT.

You can check the application is running by calling the healthcheck endpoint with:

```
curl localhost:{$PORT}/admin/healthcheck
```

### `check`

You can run the VSP with the `check` command to confirm that your configuration file is valid. For example, to check that `verify-service-provider.yml` is valid, run:

```
./bin/verify-service-provider check verify-service-provider.yml
```

## Configure

The VSP comes with a default [YAML configuration file](https://github.com/alphagov/verify-service-provider/blob/master/verify-service-provider.yml)
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

If you are using the legacy setup involving a [Matching Service Adapter (MSA)](https://github.com/alphagov/verify-matching-service-adapter), additional environment variables apply. [Contact the Verify team][contact-verify] if you need to use the MSA with VSP 2.0.0 or above.

The VSP is a Dropwizard application, so you can also configure it with the [options provided by Dropwizard][dropwizard]. Check the [VSP release notes][release-notes] to find out which Dropwizard version was used when building the release you're using.

## Contribute to the Verify Service Provider

If you wish to contribute to the development of the VSP then follow the [development
guide](CONTRIBUTING.md).

## Support and raising issues

If you think you have discovered a security issue in this code please email [disclosure@digital.cabinet-office.gov.uk](mailto:disclosure@digital.cabinet-office.gov.uk) with details.

For non-security related bugs and feature requests please [raise an issue](https://github.com/alphagov/verify-service-provider/issues/new) in the GitHub issue tracker.

## Licensing
[MIT License][mit-license]


[tech-docs]: https://www.docs.verify.service.gov.uk
[key-rotation]: https://www.docs.verify.service.gov.uk/maintain-your-connection/rotate-keys/
[vsp-get-started]: https://www.docs.verify.service.gov.uk/get-started/
[vsp-api-reference]: https://alphagov.github.io/verify-service-provider/api/verify-service-provider-api.swagger.html
[vsp-api-spec]: /docs/api/verify-service-provider-api.swagger.yml
[identity-dataset]: /src/main/resources/default-test-identity-dataset.json
[release-notes]: /RELEASE_NOTES.md
[contact-verify]: https://www.verify.service.gov.uk/support/
[dropwizard]: https://www.dropwizard.io
[configuration]: /README.md#configure
[mit-license]: https://github.com/alphagov/verify-service-provider/blob/master/LICENSE
