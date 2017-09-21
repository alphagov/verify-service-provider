---
title: Verify Service Provider v0.3.0
language_tabs: []
toc_footers: []
includes: []
search: true
highlight_theme: darkula
---

# Verify Service Provider v0.3.0

> Scroll down for example requests and responses.

This a proposed API for the Verify Serivce Provider


# Verify Service Provider API

## POST /generate-request

Generate an authn request

### Parameters

Parameter|In|Type|Description
---|---|---|---|---|
body|body|[RequestGenerationBody](#schemarequestgenerationbody)|No description (required)
» levelOfAssurance|body|string|Level of assurance requested by the Service


#### Enumerated Values

|Parameter|Value|
|---|---|
» levelOfAssurance|LEVEL_1|
» levelOfAssurance|LEVEL_2|

> Example request body

```json
{
  "levelOfAssurance": "LEVEL_1"
}
```
### Responses

Status|Meaning|Description|Schema
---|---|---|---|
200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|An object containing a SAML request.|[RequestResponseBody](#schemarequestresponsebody)
422|[Unprocessable Entity](https://tools.ietf.org/html/rfc2518#section-10.3)|An error due to a JSON request in an invalid format (e.g. missing mandatory parameters).|[ErrorMessage](#schemaerrormessage)
500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|An error due to an internal server error.|[ErrorMessage](#schemaerrormessage)

<aside class="success">
This operation does not require authentication
</aside>

> Example responses

```json
{
  "samlRequest": "string",
  "requestId": "string",
  "ssoLocation": "string"
}
```
```json
{
  "code": 422,
  "message": "Some error message"
}
```
```json
{
  "code": 500,
  "message": "Some error message"
}
```

## POST /translate-response

Create a translated response
### Parameters

Parameter|In|Type|Description
---|---|---|---|---|
body|body|[TranslateSamlResponseBody](#schematranslatesamlresponsebody)|An object containing a details of a SAML Authn response. (required)
» samlResponse|body|string(byte)|A SAML Authn response as a base64 string. (required)
» requestId|body|string(byte)|A token that was generated for the original SAML Authn request. The token is used to verify that the request and response are from the same browser. (required)
» levelOfAssurance|body|string|Level of assurance the user authenticated with. (required)


#### Enumerated Values

|Parameter|Value|
|---|---|
» levelOfAssurance|LEVEL_1|
» levelOfAssurance|LEVEL_2|

> Example request body

```json
{
  "samlResponse": "string",
  "requestId": "string",
  "levelOfAssurance": "LEVEL_1"
}
```
### Responses

Status|Meaning|Description|Schema
---|---|---|---|
200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Response contains an object with details of a translated SAML response.|[TranslatedResponseBody](#schematranslatedresponsebody)
400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|an error due to a problem with translating the Response|[ErrorMessage](#schemaerrormessage)
422|[Unprocessable Entity](https://tools.ietf.org/html/rfc2518#section-10.3)|An error due to a JSON request in an invalid format (e.g. missing mandatory parameters).|[ErrorMessage](#schemaerrormessage)
500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|an error due to an internal server error|[ErrorMessage](#schemaerrormessage)

<aside class="success">
This operation does not require authentication
</aside>

> Example responses

```json
{
  "scenario": "SUCCESS_MATCH",
  "pid": "string",
  "levelOfAssurance": "LEVEL_1",
  "attributes": {
    "firstName": {
      "value": "string",
      "verified": true
    },
    "middleName": {
      "value": "string",
      "verified": true
    },
    "surname": {
      "value": "string",
      "verified": true
    },
    "dateOfBirth": {
      "value": "string",
      "verified": true
    },
    "address": {
      "value": {
        "lines": [
          "string"
        ],
        "postCode": "string",
        "internationalPostCode": "string",
        "uprn": "string",
        "fromDate": "string",
        "toDate": "string"
      },
      "verified": true
    },
    "cycle3": "string"
  }
}
```
```json
{
  "code": 400,
  "message": "Some error message"
}
```
```json
{
  "code": 422,
  "message": "Some error message"
}
```
```json
{
  "code": 500,
  "message": "Some error message"
}
```

# Examples

## ExpectedLevelOfAssurance

<a name="schemaexpectedlevelofassurance"></a>

```json
"LEVEL_1" 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
simple|string|false|Level of assurance requested by the Service



## ReceivedLevelOfAssurance

<a name="schemareceivedlevelofassurance"></a>

```json
"LEVEL_1" 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
simple|string|false|Level of assurance the user authenticated with.



## Scenario

<a name="schemascenario"></a>

```json
"SUCCESS_MATCH" 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
simple|string|false|No description



## RequestGenerationBody

<a name="schemarequestgenerationbody"></a>

```json
{
  "levelOfAssurance": "LEVEL_1"
} 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
levelOfAssurance|string|false|Level of assurance requested by the Service


#### Enumerated Values

|Property|Value|
|---|---|
levelOfAssurance|LEVEL_1|
levelOfAssurance|LEVEL_2|


## RequestResponseBody

<a name="schemarequestresponsebody"></a>

```json
{
  "samlRequest": "string",
  "requestId": "string",
  "ssoLocation": "string"
} 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
samlRequest|string(byte)|true|SAML authn request string as a base64 string
requestId|string(byte)|true|A token that identifies the authn request. This can be used to later verify that the request and response have passed through the same browser.
ssoLocation|string(url)|true|The url for Verify HUB SSO. The entrypoint for saml authn flow.



## TranslateSamlResponseBody

<a name="schematranslatesamlresponsebody"></a>

```json
{
  "samlResponse": "string",
  "requestId": "string",
  "levelOfAssurance": "LEVEL_1"
} 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
samlResponse|string(byte)|true|A SAML Authn response as a base64 string.
requestId|string(byte)|true|A token that was generated for the original SAML Authn request. The token is used to verify that the request and response are from the same browser.
levelOfAssurance|string|true|Level of assurance the user authenticated with.


#### Enumerated Values

|Property|Value|
|---|---|
levelOfAssurance|LEVEL_1|
levelOfAssurance|LEVEL_2|


## TranslatedResponseBody

<a name="schematranslatedresponsebody"></a>

```json
{
  "scenario": "SUCCESS_MATCH",
  "pid": "string",
  "levelOfAssurance": "LEVEL_1",
  "attributes": {
    "firstName": {
      "value": "string",
      "verified": true
    },
    "middleName": {
      "value": "string",
      "verified": true
    },
    "surname": {
      "value": "string",
      "verified": true
    },
    "dateOfBirth": {
      "value": "string",
      "verified": true
    },
    "address": {
      "value": {
        "lines": [
          "string"
        ],
        "postCode": "string",
        "internationalPostCode": "string",
        "uprn": "string",
        "fromDate": "string",
        "toDate": "string"
      },
      "verified": true
    },
    "cycle3": "string"
  }
} 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
scenario|string|true|No description
pid|string(byte)|true|A unique identifier that can identify a user against an internal record.
levelOfAssurance|string|false|Level of assurance the user authenticated with.
attributes|[Attributes](#schemaattributes)|false|An object containing user attributes
» firstName|object|false|No description
»» value|string|false|No description
»» verified|boolean|false|No description
» middleName|object|false|No description
»» value|string|false|No description
»» verified|boolean|false|No description
» surname|object|false|No description
»» value|string|false|No description
»» verified|boolean|false|No description
» dateOfBirth|object|false|No description
»» value|string|false|Format yyyy-MM-dd
»» verified|boolean|false|No description
» address|object|false|No description
»» value|[Address](#schemaaddress)|false|An object describing the address fields of a user
»»» postCode|string|false|No description
»»» internationalPostCode|string|false|No description
»»» uprn|string|false|No description
»»» fromDate|string|false|Format yyyy-MM-dd
»»» toDate|string|false|Format yyyy-MM-dd
»»» lines|[string]|false|No description
»» verified|boolean|false|No description
» cycle3|string|false|No description


#### Enumerated Values

|Property|Value|
|---|---|
scenario|SUCCESS_MATCH|
scenario|ACCOUNT_CREATION|
scenario|NO_MATCH|
scenario|CANCELLATION|
scenario|AUTHENTICATION_FAILED|
scenario|REQUEST_ERROR|
levelOfAssurance|LEVEL_1|
levelOfAssurance|LEVEL_2|


## ErrorMessage

<a name="schemaerrormessage"></a>

```json
{
  "code": 0,
  "message": "string"
} 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
code|number|true|No description
message|string|true|No description



## Attributes

<a name="schemaattributes"></a>

```json
{
  "firstName": {
    "value": "string",
    "verified": true
  },
  "middleName": {
    "value": "string",
    "verified": true
  },
  "surname": {
    "value": "string",
    "verified": true
  },
  "dateOfBirth": {
    "value": "string",
    "verified": true
  },
  "address": {
    "value": {
      "lines": [
        "string"
      ],
      "postCode": "string",
      "internationalPostCode": "string",
      "uprn": "string",
      "fromDate": "string",
      "toDate": "string"
    },
    "verified": true
  },
  "cycle3": "string"
} 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
firstName|object|false|No description
» value|string|false|No description
» verified|boolean|false|No description
middleName|object|false|No description
» value|string|false|No description
» verified|boolean|false|No description
surname|object|false|No description
» value|string|false|No description
» verified|boolean|false|No description
dateOfBirth|object|false|No description
» value|string|false|Format yyyy-MM-dd
» verified|boolean|false|No description
address|object|false|No description
» value|[Address](#schemaaddress)|false|An object describing the address fields of a user
»» postCode|string|false|No description
»» internationalPostCode|string|false|No description
»» uprn|string|false|No description
»» fromDate|string|false|Format yyyy-MM-dd
»» toDate|string|false|Format yyyy-MM-dd
»» lines|[string]|false|No description
» verified|boolean|false|No description
cycle3|string|false|No description



## Address

<a name="schemaaddress"></a>

```json
{
  "lines": [
    "string"
  ],
  "postCode": "string",
  "internationalPostCode": "string",
  "uprn": "string",
  "fromDate": "string",
  "toDate": "string"
} 
```

### Properties

Name|Type|Required|Description
---|---|---|---|
postCode|string|false|No description
internationalPostCode|string|false|No description
uprn|string|false|No description
fromDate|string|false|Format yyyy-MM-dd
toDate|string|false|Format yyyy-MM-dd
lines|[string]|false|No description





