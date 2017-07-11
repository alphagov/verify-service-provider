Verify Service Provider
=======================

Verify Service Provider is a Service that provides support for the communication
between Verify Hub and a Relying Party.

Please refer to the [techinical onboarding guide](https://alphagov.github.io/rp-onboarding-tech-docs/) to understand how services can technically integrate with Verify.

Setup
-----

### Download

Release versions of the Verify Service Provider are available upon request from the Verify Support Team.

### Configure

The application accepts environment variables or a Dropwizard-style yaml configuration file for finer controls. 

It is recommended that you choose one mechanism or the other.

##### Environment variables

The following Environment Variables are defined:

* `HUB_ENTITY_ID`

  An entity id for Verify Hub

* `MSA_ENTITY_ID`

  An entity id that identifies the matching service adapter of the relying party

* `HUB_METADATA_URL`

  A Url that is used to get Metadata from Verify Hub.
  _The metadata is needed for ?_

* `MSA_METADATA_URL`

  A Url that is used to get Metadata from Matching service adapter.
  _The metadata is needed for ?_

* `MSA_TRUSTSTORE_PATH`
  A path to a truststore file for the Matching Service Adapter of the relying party.
  The truststore is needed for ?

* `MSA_TRUSTSTORE_PASSWORD`
  A password for the msa truststore.

* `HUB_TRUSTSTORE_PATH` 
  A path to a truststore file for the Verify Hub.
  _The truststore is needed for ?_

* `HUB_TRUSTSTORE_PASSWORD`
  A password for the hub truststore

* `RELYING_PARTY_TRUSTSTORE_PATH`
  A path to the truststore of the Relying Party.
  _The truststore is needed for ?_

* `RELYING_PARTY_TRUSTSTORE_PASSWORD`
  Password to the relying party truststore

* `SIGNINGKEYS_KEY`
  A base64 encoded private key that is used for signing the Authn request. With a correct
  signature, Verify Hub is able to trust that the request actually originates from the relying party.

* `SIGNINGKEYS_CERT`
  A base64 encoded public cert for signing?
  _The cert is used for?_

* `ENCRYPTIONKEYS_KEY`
  A base64 encoded private key that the relying party's Matching Service Adapter is using to encrypt
  the assertions with.
  _The key is used to decrypt the assertions._

* `ENCRYPTIONKEYS_CERT`
  A base64 encoded public cert that the relying party's Matching Service Adapter is using to encrypt
  the assertions with.
  _The cert is used to decrypt the assertions._

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

