---
title: Verify Service Provider v0.3.0
language_tabs: []
toc_footers: []
includes: []
search: true
highlight_theme: darkula
---

# Verify Service Provider v0.3.0


<p>The Verify Service Provider (VSP) API uses a `/generate-request`, `/translate-response` command format, where the response both contains all user attributes and identifies each as either ‘verified’, or 'unverified'</p>

<p>Use the `/generate-request` command to obtain a Request Response from the Verify environment.</p>

<p>Use the `/translate-response` command to translate the SAML response into JSON.</p>



# Interactions

## POST /generate-request

<p>Use the `/generate-request` command to specify the Level of Assurance (LoA) required by the Service and to provoke the environment to generate an Authntication (Authn) request.</p>

<p>POST the body to `/generate-request` to generate a SAML AuthnRequest.</p>

### Parameters

Parameter|In|Type|Description
---|---|---|---|--:--|
body|body|[RequestGenerationBody](#schemarequestgenerationbody)|-
»levelOfAssurance|body|string|User's Level of Assurance, as provided by the IDP.


#### Enumerated Values

|Parameter|Value|
|---|---|
|» levelOfAssurance|LEVEL_1|
|» levelOfAssurance|LEVEL_2|

> For Example:

```json
{
  "levelOfAssurance": "LEVEL_1"
}
```
### Responses


Status|Meaning|Description|Schema
---|---|---|---|
200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|The object received contained a valid SAML request.|[RequestResponseBody](#schemarequestresponsebody)
422|[Unprocessable Entity](https://tools.ietf.org/html/rfc2518#section-10.3)|<p>The JSON request format is invalid.</p> For example: mandatory parameters were missing.|[ErrorMessage](#schemaerrormessage)
500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|An internal server error prevented correct processing.|[ErrorMessage](#schemaerrormessage)

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

<p>The `/translate-response` command body contains three mandatory parameters, which form the SAML Authn Response.</p>   
<p>POST the body to `/translate-response` to create a translated Response message.</p>

### Parameters

Parameter|In|Type|Description
---|---|---|---|---|
body|body|[TranslateSamlResponseBody](#schematranslatesamlresponsebody)|An object that contains details of a SAML Authn response.
»samlResponse|body|string (byte)|The base 64 SAML Authn response string.
»requestId|body|string (byte)|A token generated for the original SAML Authn request, used to verify that both the request and response are from the same browser.
»levelOfAssurance|body|string|User's Level of Assurance, as supplied by the IDP.


#### Enumerated Values

|Parameter|Value|
|---|---|
|»levelOfAssurance|LEVEL_1|
|»levelOfAssurance|LEVEL_2|

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
200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|The Response contains an object that includes a valid translated SAML response.|[TranslatedResponseBody](#schematranslatedresponsebody)
400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|The Response was not translated correctly.|[ErrorMessage](#schemaerrormessage)
422|[Unprocessable Entity](https://tools.ietf.org/html/rfc2518#section-10.3)|<p>An invalid JSON request format.</p><p>For example: mandatory parameters were missing.</p>|[ErrorMessage](#schemaerrormessage)
500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|An internal server error prevented correct processing.|[ErrorMessage](#schemaerrormessage)



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
  "message": "A different error message"
}
```
```json
{
  "code": 500,
  "message": "Yet anoher error message"
}
```

# Examples

## ExpectedLevelOfAssurance

<a name="schemaexpectedlevelofassurance"></a>
<p>For example:</p>

```json
"LEVEL_1"
```

### Properties

Name|Type|Required|Description
---|---|---|---|
simple|string|false|The Level of Assurance requested by the Service.



## ReceivedLevelOfAssurance

<a name="schemareceivedlevelofassurance"></a>
<p>For example:</p>

```json
"LEVEL_1"
```

### Properties

Name|Type|Required|Description
---|---|---|---|
simple|string|false|The user's Level of Assurance, as supplied by the IDP.



## Scenario

<a name="schemascenario"></a>
<p>For example:</p>

```json
"SUCCESS_MATCH"
```

### Properties

Name|Type|Required|Description
---|---|---|---|
simple|string|false|In this case: The IDP has confirmed the user's information and this information has been found by the Local Matching Service.



## RequestGenerationBody

<a name="schemarequestgenerationbody"></a>
<p>For example:</p>

```json
{
  "levelOfAssurance": "LEVEL_1"
}
```

### Properties

Name|Type|Required|Description
---|---|---|---|
levelOfAssurance|string|false|The Level of Assurance requested by the Service.


#### Enumerated Values

|Property|Value|
|---|---|
|levelOfAssurance|LEVEL_1|
|levelOfAssurance|LEVEL_2|


## RequestResponseBody

<a name="schemarequestresponsebody"></a>
<p>If the `/generate-request` command receives a valid (200) response from the environment then the `RequestResponseBody` will contain three mandatory elements: `samlRequest`, `requestID` and `ssolocation`.</p>
<p>For example:</p>

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
samlRequest|String (byte)|True|The base 64 SAML Authn request string.
requestId|String (byte)|True|A token used to identify the Authn request and verify that the request / response pair have passed through the same browser.
ssoLocation|String (url)|True|The SAML Authn flow entrypoint (The Verify environment SSO url).






## TranslateSamlResponseBody

<a name="schematranslatesamlresponsebody"></a>
<p>The `TranslateSamlResponseBody` contains three elements: the SAML Authn response data, a token used to identify the specific request and the user LoA, as provided by the IDP.</p>

<p>For example:</p>

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
samlResponse|string (byte)|true|A SAML Authn response as a base64 string.
requestId|string (byte)|true|The token generated for the original SAML Authn request, used to confirm that the request and response are from the same browser.
levelOfAssurance|string|true|The user's Level of assurance, as provided by the IDP.


#### Enumerated Values

|Property|Value|
|---|---|
|levelOfAssurance|LEVEL_1|
|levelOfAssurance|LEVEL_2|


## TranslatedResponseBody

<a name="schematranslatedresponsebody"></a>
<p>For example:</p>

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
scenario|string|True|<p>SUCCESS_MATCH</p><p>ACCOUNT_CREATION</p><p>NO_MATCH</p><p>CANCELLATION </p><p>AuthnTICATION_FAILED</p><p>REQUEST_ERROR</p>
pid|string (byte)|True|<p>The unique code used to link a returning user with an existing local record.</p><p>NOTE: The user must apply to GOV.UK Verify using the same IDP, or a new pid is generated.</p>
levelOfAssurance|string|False|<p>User's Level of Assurance, as provided by the IDP:</p><p>LEVEL_1</p><p>LEVEL_2</p>|
[Attributes](#schemaattributes)|-|False|An object containing user attributes.
»firstName|object|False|-
»»value|string|False|-
»»verified|boolean|False|-
»middleName|object|False|-
»»value|string|False|-
»»verified|boolean|False|-
»surname|object|False|-
»»value|string|False|-
»»verified|boolean|False|-
»dateOfBirth|object|False|-
»»value|string|False|Format yyyy-MM-dd
»»verified|boolean|False|-
»address|object|False|-
»»value|[Address](#schemaaddress)|False|An object describing the user's address fields.
»»»postCode|string|False|-
»»»internationalPostCode|string|False|-
»»»uprn|string|False|-
»»»fromDate|string|False|Format yyyy-MM-dd
»»»toDate|string|False|Format yyyy-MM-dd
»»»lines|[string]|False|-
»»verified|boolean|False|-
»cycle3|string|False|-


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
code|number|true|-
message|string|true|-



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
firstName|object|False|-
» value|string|False|-
» verified|boolean|False|-
middleName|object|False|-
» value|string|False|-
» verified|boolean|False|-
surname|object|False|-
» value|string|False|-
» verified|boolean|False|-
dateOfBirth|object|False|-
» value|string|False|Format: yyyy-MM-dd
» verified|boolean|False|-
address|object|False|-
» value|[Address](#schemaaddress)|False|An object describing the user's address fields.
»» postCode|string|False|-
»» internationalPostCode|string|False|-
»» uprn|string|False|-
»» fromDate|string|False|Format yyyy-MM-dd
»» toDate|string|False|Format yyyy-MM-dd
»» lines|[string]|False|-
» verified|boolean|False|-
cycle3|string|False|-



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
postCode|string|False|-
internationalPostCode|string|False|-
uprn|string|False|-
fromDate|string|False|Format yyyy-MM-dd
toDate|string|False|Format yyyy-MM-dd
lines|[string]|False|-
