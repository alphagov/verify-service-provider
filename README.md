Verify Service Provider
=======================

Verify Service Provider is a Service that provides support for the communication
between Verify Hub and a Relying Party.

__Terminology:__
* Relying Party is a Service that needs to have users authenticated with an Identity Provider.
* Identity Provider is a Service that can provide authentication of users.
* Verify Hub acts as an Identity Provider for Relying Parties.

Installation
-------------

1. __Download and unzip__ the verify-service-provider release. Available by request from
the Verify Support Team.
   ```shell-script
   tar -xf verify-service-provider.zip -C ./
   ```

2. __Configure the application.__ The application accepts environment variables as well as a
typical yaml-configuration file. See the provided `verify-service-provider.yml` for an example
and detailed documentation.
   ```shell-script
   vim verify-service-provide/verify-service-provider.yml
   ```

3. __Spin up the application.__
   ```shell-script
   verify-service-provider/bin/verify-service-provider server ./verify-service-provide/rconfiguration/verify-service-provider.yml
   ```
   Or with only environment variables
   ```shell-script
   HUB_ENTITY_ID=http://hub-entity-id \
   MSA_ENTITY_ID=http://msa-entity-id \
   HUB_METADATA_URL=https://hub-metadata-url \
   MSA_METADATA_URL=https://msa-metadata-url \
   MSA_TRUSTSTORE_PATH=/path/to/msa/truststore \
   MSA_TRUSTSTORE_PASSWORD=password \
   HUB_TRUSTSTORE_PATH=/path/to/hub/truststore \
   HUB_TRUSTSTORE_PASSWORD=password \
   RELYING_PARTY_TRUSTSTORE_PATH=/path/to/relying-party/truststore \
   RELYING_PARTY_TRUSTSTORE_PASSWORD=password \
   SIGNINGKEYS_KEY=base64encodedPrivateKey \
   SIGNINGKEYS_CERT=base64encodedPublicCert \
   ENCRYPTIONKEYS_KEY=base64encodedPrivateKey \
   ENCRYPTIONKEYS_CERT=base64encodedPublicCert \
   dw.server.applicationConnectors[0].type=http \
   dw.server.applicationConnectors[0].port=50400 \
   dw.server.adminConnectors[0].type=http \
   dw.server.adminConnectors[0].port=50401 \
   dw.logging.level=INFO \
   dw.logging.appenders[0].type=console \
     verify-service-provider/bin/verify-service-provider server
   ```

Configuration
-------------
See the provided `verify-service-provider.yml` for an example and detailed documentation.
https://github.com/alphagov/verify-service-provider/blob/master/prototypes/prototype-0/verify-service-provider/configuration/verify-service-provider.yml

Verify Service Provider is a Dropwizard application and thus supports all configuration options
provided by Dropwizard: http://www.dropwizard.io/1.1.0/docs/manual/configuration.html

Configurations can be provided by the yml file and/or environment variables. see above for an example.

The following Environment confs are supported:
```
HUB_ENTITY_ID
# An entity id for Verify Hub

MSA_ENTITY_ID
# An entity id that identifies the matching service adapter of the relying party

HUB_METADATA_URL
# A Url that is used to get Metadata from Verify Hub.
# The metadata is needed for ?

MSA_METADATA_URL 
# A Url that is used to get Metadata from Matching service adapter.
# The metadata is needed for ?

MSA_TRUSTSTORE_PATH 
# A path to a truststore file for the Matching Service Adapter of the relying party.
# The truststore is needed for ?

MSA_TRUSTSTORE_PASSWORD  
# A password for the msa truststore.

HUB_TRUSTSTORE_PATH 
# A path to a truststore file for the Verify Hub.
# The truststore is needed for ?

HUB_TRUSTSTORE_PASSWORD 
# A password for the hub truststore

RELYING_PARTY_TRUSTSTORE_PATH  
# A path to the truststore of the Relying Party.
# The truststore is needed for ?

RELYING_PARTY_TRUSTSTORE_PASSWORD  
# Password to the relying party truststore

SIGNINGKEYS_KEY  
# A base64 encoded private key that is used for signing the Authn request. With a correct
# signature, Verify Hub is able to trust that the request actually originates from the relying party.

SIGNINGKEYS_CERT  
# A base64 encoded public cert for signing? The cert is used for?

ENCRYPTIONKEYS_KEY  
# A base64 encoded private key that the relying party's Matching Service Adapter is using to encrypt
# the assertions with. The key is used to decrypt the assertions.

ENCRYPTIONKEYS_CERT  
# A base64 encoded public cert that the relying party's Matching Service Adapter is using to encrypt
# the assertions with. The cert is used to decrypt the assertions.

dw.server.applicationConnectors[0].type
# see http://www.dropwizard.io/1.1.0/docs/manual/configuration.html#man-configuration-connectors

dw.server.applicationConnectors[0].port
# see http://www.dropwizard.io/1.1.0/docs/manual/configuration.html#man-configuration-connectors

dw.server.adminConnectors[0].type
# see http://www.dropwizard.io/1.1.0/docs/manual/configuration.html#man-configuration-connectors

dw.server.adminConnectors[0].port
# see http://www.dropwizard.io/1.1.0/docs/manual/configuration.html#man-configuration-connectors

dw.logging.level
# see http://www.dropwizard.io/1.1.0/docs/manual/configuration.html#logging

dw.logging.appenders[0].type
# see http://www.dropwizard.io/1.1.0/docs/manual/configuration.html#logging
```

Usage
-----

Verify Service Provides a REST Http Api that supports the communication between the Relying party
and Verify.

Due to how SAML works, the communication with Verify happens over the user's browser using form POSTs.
Verify Service Provider will provide the content and support for parsing the SAML messages but it is up
to the Relying party to render the form for the user. See Passport-Verify for support on doing this
with node.js. https://github.com/alphagov/verify-service-provider/tree/master/prototypes/prototype-0/stub-rp/passport-verify

__When The authentication flow begins__, Verify Service Provider is used to create a SAML AuthnRequest.

![Request flow diagram](https://github.com/alphagov/verify-service-provider/blob/master/prototypes/prototype-0/docs/diagrams/request_flow.png)

```shell-script
curl -XPOST \
  -H "Content-Type: application/json" \
  -d '{"levelOfAssurance":"LEVEL_2"}' \
  --verbose \
  localhost:50400/generate-request

> POST /generate-request HTTP/1.1
> Host: localhost:50400
> User-Agent: curl/7.43.0
> Accept: */*
> Content-Type: application/json
> Content-Length: 30
>
* upload completely sent off: 30 out of 30 bytes
< HTTP/1.1 200 OK
< Date: Fri, 07 Jul 2017 09:31:47 GMT
< Content-Type: application/json
< Content-Length: 107
<
* Connection #0 to host localhost left intact
{
  "samlRequest": "some-saml",
  "secureToken": "some-secure-token",
  "location": "http://localhost:50410/SAML2/SSO"
}
```

__When The authentication flow ends__, Verify Service Provider is used to transform the SAML Response message into json. 

![Response flow diagram](https://github.com/alphagov/verify-service-provider/blob/master/prototypes/prototype-0/docs/diagrams/response_flow.png)

```shell-script
SUCCESS_SCENARIO=`echo '{"scenario":"SUCCESS_MATCH","levelOfAssurance":"LEVEL_2","pid":"some-pid"}' | base64`
curl -XPOST \
  -H "Content-Type: application/json" \
  -d "{\"samlResponse\":\"$SUCCESS_SCENARIO\", \"secureToken\":\"some-secure-token\"}" \
  localhost:50400/translate-response

*   Trying ::1...
* Connected to localhost (::1) port 50400 (#0)
> POST /translate-response HTTP/1.1
> Host: localhost:50400
> User-Agent: curl/7.43.0
> Accept: */*
> Content-Type: application/json
> Content-Length: 154
>
* upload completely sent off: 154 out of 154 bytes
< HTTP/1.1 200 OK
< Date: Fri, 07 Jul 2017 09:46:06 GMT
< Content-Type: application/json
< Content-Length: 92
<
* Connection #0 to host localhost left intact
{
  "scenario": "SUCCESS_MATCH",
  "pid": "some-pid",
  "levelOfAssurance": "LEVEL_2",
  "attributes": null
}
```

API
---

A detailed API documentation can be found in a swagger file at https://github.com/alphagov/verify-service-provider/blob/master/prototypes/prototype-0/docs/verify-service-provider-api.swagger.yml

__POST /generate-request__

Generates a base64 encoded SAML Authn Request that can be used to begin the authentication flow.

```
Content-Type: application/json
POST /generate-request
{
  // an enum of expected level of assurance
  // LEVEL_1 | LEVEL_2 | LEVEL_3
  "levelOfAssurance": "LEVEL_1"
}

200 Ok
{
  // base64 encoded string that is supposed to contain saml
  "samlRequest": "c29tZS1zYW1sCg==" 

  // a token uniquely identifying the authn request. This is used later to verify that the request and response originate from the same browser.
  "secureToken": "some-secure-token"

  // The url for Verify HUB SSO. The entrypoint for saml authn flow.
  "location": "http://SSOurl"
}

400 Bad Request
500 Internal Server Error
{
  // an enum describing the error reason
  // BAD_REQUEST | INTERNAL_SERVER_ERROR | AUTHENTICATION_FAILED | NO_MATCH | CANCELLATION
  "reason": "BAD_REQUEST"

  // a message for further description of the error
  "message": "something went wrong"
}
```

__POST /translate-response__

Translates a base64 encoded `samlResponse` into json that describes the desired scenario. A different
Response is provided depending on the scenario provided in the `samlResponse` parameter. See Stub Verify Hub
for further details on the scenarios https://github.com/alphagov/verify-service-provider/tree/master/prototypes/prototype-0/stub-verify-hub.

prototype-0 version of Verify Service Provider has a slightly modified api from the final version.
It is made to work hand in hand with stub-verify-hub. While the API is similar to the final
design, the content of base64 encoded `samlResponse` is expected to be json instead of saml.

See Stub Verify Hub for further description of the possible scenarios and samlResponses: https://github.com/alphagov/verify-service-provider/tree/master/prototypes/prototype-0/stub-verify-hub

```
Content-Type: application/json
POST /translate-response
{
  // a base64 encoded string of json that describes the scenario
  // this will be actual saml response in the final version of Verify Service Provider
  // see Stub Verify Hub for description of the scenarios and example responses
  "samlResponse": "eyJzY2VuYXJpbyI6IlNVQ0NFU1NfTUFUQ0giLCJsZXZlbE9mQXNzdXJhbmNlIjoiTEVWRUxfMiIsInBpZCI6InNvbWUtcGlkIn0K"

  // a secure token generated with /generate-request 
  "secureToken": "some-secure-token"
}

200 Ok
{
  // A personal identifier for the user
  "pid": "some-pid"

  // Level Of Assurance provided by the Identity Provider
  // This describes how certain the Identity Provider is that the user is who
  // she claims to be.
  // LEVEL_1 | LEVEL_2 | LEVEL_3
  "levelOfAssurance": "LEVEL_2"

  // An object containing the user attributes if the user is not
  // previously known by the Relying Party.
  // Only some fields might be present.
  "attributes": {
    "firstName": "some-firstName",
    "firstNameVerified: true,
    "middleName": "some-middle-name",
    "middleNameVerified: true,
    "surname": "some-surname",
    "surnameVerified": true,
    // ISO string
    "dateOfBirth": "1990-01-01",
    "dateOfBirthVerified: true,
    "address": {
      "verified": true,
      "lines": [
        "addressLine1",
        "addressLine2"
      ],
      "postCode: "some-post-code",
      "internationalPostCode": "some-post-code",
      "uprn": "some-uprn"
    },
    "cycle3": "some-cycle3-name"
  }
}

400 Bad Request
401 Authentication Failed
500 Internal Server Error
{
  // an enum describing the error reason
  // BAD_REQUEST | INTERNAL_SERVER_ERROR | AUTHENTICATION_FAILED | NO_MATCH | CANCELLATION
  "reason": "BAD_REQUEST"

  // a message for further description of the error
  "message": "something went wrong"
}

```


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

