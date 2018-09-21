#!/usr/bin/env bash
set -e

if [ ! $# -eq 3 ] ; then
	echo "For a matching journey this script must include the environment, the --matching flag and a PID"
	echo "For example: $ ./init-compliance-tool-for-environment.sh <environment> --matching <persistentId>"
	echo "For a non-matching journey this script must include the environment, the --nonmatching flag and the filepath to the matching dataset json"
	echo "For example: $ ./init-compliance-tool-for-environment.sh <environment> --nonmatching filepath/to/matching-dataset-for-rp.json"
	echo "please try again, currently allowed environments are 'local' and 'dev'"
	exit 1
fi

COMPLIANCE_TOOL_INIT_URL_SLUG="relying-party-service-test-run"
if [ ${2-""} == "--matching" ] ; then
  COMPLIANCE_TOOL_INIT_URL_SLUG="service-test-data"
	echo "setting persistentID as $3"
	PID=$3
elif [ ${2-""} =~ "--nonmatching" ] ; then
  echo "You must specify --matching or --nonmatching"
	exit 1
fi

VSP_SIGNING_CERT=`openssl x509 -in ../test-keys-and-certs/vsp-signing.crt -outform DER | base64`
VSP_ENCRYPTION_CERT=`openssl x509 -in  ../test-keys-and-certs/vsp-encryption.crt -outform DER | base64`

case $1 in
  	'local-service-using-local-compliance')
		echo "configuring local compliance tool running on localhost:50270 for Relying Party running on localhost:3200"
		SERVICE_ENTITY_ID="http://verify-service-provider-local"
		ASSERTION_CONSUMER_SERVICE_URL="http://localhost:3200/verify/response"
		COMPLIANCE_TOOL_INIT_URL="http://localhost:50270/$COMPLIANCE_TOOL_INIT_URL_SLUG"
    MATCHING_SERVICE_ENTITY_ID="http://www.test-rp-ms.gov.uk/SAML2/MD"
    MATCHING_SERVICE_SIGNING_KEY=`cat ../../ida-hub-acceptance-tests/keys/test_rp_msa_signing.pk8 | base64`
  ;;
    'local-service-using-remote-compliance')
    echo "configuring remote compliance tool running on reference for Relying Party running on localhost:3200"
    SERVICE_ENTITY_ID="http://verify-service-provider-local"
    ASSERTION_CONSUMER_SERVICE_URL="http://localhost:3200/verify/response"
    COMPLIANCE_TOOL_INIT_URL="https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/service-test-data"
    MATCHING_SERVICE_ENTITY_ID="https://verify-service-provider-stub-msa"
    MATCHING_SERVICE_SIGNING_KEY=`cat ../../ida-hub-acceptance-tests/keys/test_rp_msa_signing.pk8 | base64`
  ;;
  'dev')
    echo "configuring remote compliance tool running on reference for Relying Party running on dev (on PaaS)"
    SERVICE_ENTITY_ID="http://verify-service-provider-dev-service"
    ASSERTION_CONSUMER_SERVICE_URL="https://passport-verify-stub-relying-party-dev.cloudapps.digital/verify/response"
    COMPLIANCE_TOOL_INIT_URL="https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/$COMPLIANCE_TOOL_INIT_URL_SLUG"
    MATCHING_SERVICE_ENTITY_ID="https://verify-service-provider-stub-msa"
    MATCHING_SERVICE_SIGNING_KEY="MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDQaRaD5w0JythQx5tm26o/V7pmCCZCKydK0dQN73loe39MmbLiKHQlif+h74qC45dBY5tPeq0r1qmTN6HxudADVeQHNHmLDwVJLeJJ8+8GbzZcQPCsvEERY0b0/PVINV9qdGjjeqD4uK15VVWgNwx0Lo4SCufKRZKrazfJtYWk/MrBaoYQ7MEoElaHjMfjMiCsO4YUXi114b2I8fvd0ksz9iFELHZPhVsG19Kprm/ntofnJRJa/v9unK0xLNUt1h22l4jFDbddg5/2qm5oDIy7TgaYR6oT1oGE9TNJ40oo0CwMIMCSQwzDhUBYMAsdBywZQkjPxI6vdGXXfRJ4JHeZAgMBAAECggEABsrmCPaG3kLWOgvHbNrL+bSKRrkNRirR5QZhuvKLKnSb+Yso4WYgIvkUc9qy0QFJ9L+iWDzPWBZvAHVXueEsfm9WN0XmkDm1GmV8cpyYtcT6KJeVQIwCMubhcSqtc+UJbxbMNF3S0UhznvjBCa/BoCfESaJBW75YwW2FK/XWnUHw5N6YJ2T1joZD5FfBMKdnwSR/7C0IH1z4D9IW/cg9RM3VZNjxC9UU5a5E1ndoCdLc7MxBm+zH8M1dC+NOMhqR/iFMcwrF8LsaU3EtbnisVYvbZEQremb+WVrfL+n0PhFMUcoCFkC5eSukRPQ3bN931gUC7EcFlSaeHiPRsaD8YQKBgQD0r/AqfZKnbYCp9vg1rCJU/FiQT/xBz7sWowPQow155iN4jUT22nHiwPnn75qbgRXBiHQkfhUtIWouURn3nzMp81m38cLvkNNUHltAPmXxbANf5nI624uGEKXnCX5uD5efe/cUNLKGA7fnONOhph2m0rKkjAqyqEuOUfU6zC8Q6wKBgQDaC8lWEFeW6g0R4pmpLWb/ZYyKTOW4TkoyTyQBDsGTVhSNi/UsbaFI9egpcLMrgSByCTdaL5AwjcKS3DJBlUGfTd+LRXuPxOSp+111J/XZ2GCXVszV+5eYp/BPwLySRKlmzMSI28oWV6N3u5ZfvvR2PGRh8+mTniWS4GJ4GPDYiwKBgEAipZbdl0UfZKwoOeMHnXAdPLGG5Z3ybx192RAkzPF4qy98B+mUVGmVH2v119aOvT8fHyI5kh9kNMqzI1VOe0CxsoCOdAQLN/lCg7SRJnNjVncaljJrPWUElBe821DJ8XoyKg83yNtruhZ3RLGIMxl4/K44rs0pY7SIMvkYb/XFAoGBAINMpaiVnqjZt5UVhsJA/My+Mar2Mz6Qpk01KtEYOainJSk3JiPiwERXD74khz+jOg5xTkuYaJNUSd51ii3D2wg6tGoBJS6luaxCGTz7GyhbC48WTbJtFhRuzF66CNNrVTb6Bz8CWuapT15CL4LoUf0A0NHLNtQVXzras3DuU9mRAoGBAJfTmPTFnoEkygCo5qAFB/4sm4DYRLIVztImAeD9cwkJG8XEVgj49VWH1Xv8chtavhBnayounHzRfAe/5s+4V12GT9kEGxAnSymqJrlbtMFxZGtNUJpIrslt4KbXnoZCnk/H+2xDog7K9wCxup9PWkUrrNDVu630UgMnTM3wvn42"
  ;;
  *)
    echo "unrecognised environment option: $1 - exiting"
    exit 1
esac

if [ ${2-""} == "--matching" ] ; then
	curl $COMPLIANCE_TOOL_INIT_URL --data @- --header 'Content-Type: application/json' <<EOJSON
	{
	  "serviceEntityId":"$SERVICE_ENTITY_ID",
	  "assertionConsumerServiceUrl":"$ASSERTION_CONSUMER_SERVICE_URL",
	  "signingCertificate": "$VSP_SIGNING_CERT",
	  "encryptionCertificate": "$VSP_ENCRYPTION_CERT",
	  "expectedPID":"$PID",
	  "matchingServiceEntityId":"$MATCHING_SERVICE_ENTITY_ID",
	  "matchingServiceSigningPrivateKey":"$MATCHING_SERVICE_SIGNING_KEY",
	  "useSimpleProfile": false,
	  "userAccountCreationAttributes":[
	    "FIRST_NAME",
	    "FIRST_NAME_VERIFIED",
	    "MIDDLE_NAME",
	    "MIDDLE_NAME_VERIFIED",
	    "SURNAME",
	    "SURNAME_VERIFIED",
	    "DATE_OF_BIRTH",
	    "DATE_OF_BIRTH_VERIFIED",
	    "CURRENT_ADDRESS",
	    "CURRENT_ADDRESS_VERIFIED",
	    "ADDRESS_HISTORY",
	    "CYCLE_3"
	  ]
	}
EOJSON
else
	jsonData=$(cat <<EOJSON
	{
	  "serviceEntityId":"$SERVICE_ENTITY_ID",
	  "assertionConsumerServiceUrl":"$ASSERTION_CONSUMER_SERVICE_URL",
	  "signingCertificate": "MIIC0jCCAboCCQDx9/z+IdVV6DANBgkqhkiG9w0BAQsFADAqMSgwJgYDVQQDEx9WZXJpZnkgU2VydmljZSBQcm92aWRlciBTaWduaW5nMCAXDTE3MDgxMDE1NTUwN1oYDzIxMTcwNzE3MTU1NTA3WjAqMSgwJgYDVQQDEx9WZXJpZnkgU2VydmljZSBQcm92aWRlciBTaWduaW5nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvSaDKeyxVC+llVxgvifUGfBi0BQF24Un7Js/mtLt6DSfTtiNtLwkbJLb/Y1hEZtZee5jz5WpE+N3fGL3CFF1wy+ezVPOSEMcP5AJ376dJ4QkMglaDVNE49QHxM58dLdRdbSY/CwGTcyR8ZDOHC6scdgdPjDoM3EhyA79EaNycalxLQv9m01YgAjCzaE+afvO51l8A6wxDoadMZG68Paz9k4PmJyg/zcv3VrsvTL5wMs9qX1UW1F7YQLTEEVLkS04oGIsxWEFhtTDv2Vsif7W2NJLqTjoKrTLKHn7N+aEMGMskJVc7gFFm0EwTKkIt/PrE1BXW7f9iNMvBs39PnkkyQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQC47LMMcGr2QXtJlyQsKZGX4SsSzdpVr/1Y5V1soeeRO39jKiYH12z1lh+8OlHF/NcbawEAKZnqkYS0Ka1mE5uKlDmwZ5GtCPYSmD/UVu54zdfrEMhsh7jTM0iong69wx8SfEat7XCt999EFEcIj4OC31X93JX6O0nNx17lzJP+W0jMX8GJSlFeNIYX2r8F83PimC9et949KyEBP0vakqp65Fg1bO9NNlIB17jR4C4OypLD+5gDxshMq9xngqithCNqPpByuS7mmJ+9S/zAIPHhyyYkoHfvLGmc0N6QSqEfUs16RRTLCquumitvUNZxs2Gvqm/RDRfVqviqNRKxlpWe",
	  "encryptionCertificate": "MIIC2DCCAcACCQCJmzLC9XOdwzANBgkqhkiG9w0BAQsFADAtMSswKQYDVQQDEyJWZXJpZnkgU2VydmljZSBQcm92aWRlciBFbmNyeXB0aW9uMCAXDTE3MDgxMDE1NTgwNFoYDzIxMTcwNzE3MTU1ODA0WjAtMSswKQYDVQQDEyJWZXJpZnkgU2VydmljZSBQcm92aWRlciBFbmNyeXB0aW9uMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwCshPm6j+SBjC6i4kSdlFH0H8pLEOe/8gvrUStN0fCsO7fatgyIoXMOQwtTkuAW9cBL0PYPkKBfpFakZmMyZZPvakqH+QrhAaCFVa0uJmlQ4DEVzuURF0wbkz4XZqOoyomLQATtv9zpb2cHfTCS87ejIQ0/wOCHdiJOCPBcgxGjAL8ztEEuAAypgpTrROoThtxTh4FqdOeqiS03NAZcxi7rMbQ8O20tE4MUIkBmFU2yB+UgW33bOSa1in5MwEdvFT4buP8R+rEZ4cD3+K1TeDpT4ssYCCdo4UC/W9+uSIiTOL1UtmBV8kAnUvDg4LNBe5mJ9M1zZvQlXrgCLNd/noQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBEsWIcTeU0OlN2bjKVwtfsB5Eb3Mri/wsYc17Rv0rHSyeudiGphlmHYzPvFBz0dMO6YKjEOrX5a6tkN+PQE9otcjBmY4Ice+LlrJ4Vmu1dsG60S/e5lZCYE2BOY0Jpdjk4aJQeMOfyDrJUmjUWzmx+U1IdK6uuatS7+iJdR/z1FwZt+aEqaO+oVxFuBU32z+x/B/l3c5IqIuZePaYcWBFaCQzPAig9EXO3nNwcwRiDWd11nhWshVr5pTMRgSqLXMl/j4RLvr4FYg3sV5MqRqxNBjW5hl64UNfIzmD794yO8Spqgn4BZouOMCZ7Zo+GE/fcZVLhDwRMU3qgmHZBS8jk",
	  "matchingDatasetJson":\$jsonFromFile
	}
EOJSON
)
	jsonDataIncludingFileData="$( jq -n --argjson jsonFromFile "$(cat $3)" "$jsonData" )"
	curl $COMPLIANCE_TOOL_INIT_URL --data @- --header 'Content-Type: application/json' <<JSON
$jsonDataIncludingFileData
JSON
fi
echo
