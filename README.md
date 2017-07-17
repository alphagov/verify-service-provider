Verify Service Provider
=======================

Verify Service Provider is a Service that provides support for the communication
between Verify Hub and a Relying Party.

Please refer to the [techinical onboarding guide](https://alphagov.github.io/rp-onboarding-tech-docs/) to understand how services can technically integrate with Verify.

__NOTE__: The Verify Service Provider is currently in a prototyping phase of development. The API and configuration options
may change during development.


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

export PORT=... # The TCP port where the application will listen for HTTP traffic
export LOG_LEVEL=... # The threshold level for logs to be written (e.g. DEBUG, INFO, WARN, or ERROR) (default: INFO)
export MSA_ENTITY_ID=... # The SAML Entity Id that identifies the Relying Party's Matching Service Adapter
export MSA_METADATA_URL=... # The URL to the Matching Service Adapter's SAML metadata.
export HUB_ENTITY_ID=... # The SAML Entity Id that identifies the Verify Hub (default: https://signin.service.gov.uk)
export HUB_METADATA_URL=... # The URL to the Verify Hub's SAML metadata. (default: https://www.signin.service.gov.uk/SAML2/metadata/federation
export SECURE_TOKEN_KEY=... # A random string value used as a key to generate tokens
export SAML_SIGNING_KEY=... # A base64 encoded RSA private key that is used for signing the request to Verify
export SAML_PRIMARY_ENCRYPTION_KEY=... # A primary base64 encoded PKCS8 RSA private key that is used to decrypt encrypted SAML Assertions (see "Generating keys for testing")
export SAML_SECONDARY_ENCRYPTION_KEY=... # (Optional) A secondary base64 encoded PKCS8 RSA private key that is used to decrypt encrypted SAML Assertions that will be used during certificate rotation events (see "Generating keys for testing")

# Run the application with the above variables set
./bin/verify-service-provider
```

#### Generating keys for testing

If you have openssl installed you can generate a private key in the correct format with:

```
openssl genrsa -des3 -passout pass:x -out "key-name.pass.key" 2048
openssl rsa -passin pass:x -in "key-name.pass.key" -out "key-name.key"
openssl pkcs8 -topk8 -inform PEM -outform DER -in "key-name.key" -out "key-name.pk8" -nocrypt

# This command will print a base64 encoded PKCS8 RSA private key to standard out
openssl base64 -in key-name.pk8 -out key-name.pk8.base64

# Strip the newlines from the base64 encoded file and print to standard out
tr -d '\n' < key-name.pk8.base64
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

If you're not using one of these frameworks you can interact with the HTTP API directly.
See the [swagger documentation](https://alphagov.github.io/verify-service-provider) for details.

## Advanced Configuration

Yaml based configuration provides more fine grained controls over the application. For example, you are able to configure HTTPS endpoints.

See the reference configuration file available at [verify-service-provider.yml](
https://github.com/alphagov/verify-service-provider/blob/master/prototypes/prototype-0/verify-service-provider/configuration/verify-service-provider.yml
)

Verify Service Provider is a Dropwizard application and thus supports all configuration options
provided by Dropwizard: http://www.dropwizard.io/1.1.0/docs/manual/configuration.html

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

Remember to run acceptance tests after changes https://github.com/alphagov/verify-service-provider/tree/master/prototypes/prototype-0/acceptance-tests

__Build a distribution__
```
./gradlew distZip
```

The distribution zip can be found at `build/distributions`.
