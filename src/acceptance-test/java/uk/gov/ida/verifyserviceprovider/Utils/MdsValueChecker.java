package uk.gov.ida.verifyserviceprovider.Utils;

import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingAddress;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MdsValueChecker {

    public static void checkMdsValueOfAttribute(
            String attributeName,
            String expectedValue,
            boolean expectedIsVerified,
            String expectedFromDateString,
            String expectedToDateString,
            JSONObject attributes
    ) {
        JSONObject attribute = attributes.getJSONObject(attributeName);
        checkMdsValueInJsonObject(attribute, expectedValue, expectedIsVerified, expectedFromDateString, expectedToDateString);
    }

    public static void checkMdsValueOfAttributeWithoutDates(
            String attributeName,
            String expectedValue,
            boolean expectedIsVerified,
            JSONObject attributes
    ) {
        JSONObject attribute = attributes.getJSONObject(attributeName);
        checkMdsValueInJsonObject(attribute, expectedValue, expectedIsVerified);
    }

    public static void checkMdsValueOfAddress(
            int index,
            JSONObject attributes,
            MatchingAddress matchingAddress
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fromDate = matchingAddress.getFrom().toLocalDate().atStartOfDay().format(formatter);
        String toDate = Optional.ofNullable(matchingAddress.getTo())
                .map((dt) -> dt.toLocalDate().atStartOfDay().format(formatter))
                .orElse(null);
        checkMdsValueOfAddress(
                index,
                matchingAddress.getLines(),
                matchingAddress.getPostCode(),
                matchingAddress.getInternationalPostCode(),
                matchingAddress.getUprn(),
                fromDate,
                toDate,
                attributes,
                matchingAddress.isVerified()
        );
    }

    public static void checkMdsValueOfAddress(
            int index,
            List<String> lines,
            Optional<String> postcode,
            Optional<String> internationalPostcode,
            Optional<String> uprn,
            String expectedFromDateString,
            String expectedToDateString,
            JSONObject attributes,
            boolean expectedIsVerified
    ) {
        JSONArray addresses = attributes.getJSONArray("addresses");
        JSONObject addressMdsValue = addresses.getJSONObject(index);
        JSONObject addressValue = addressMdsValue.getJSONObject("value");

        checkMdsMetadataInJsonObject(addressMdsValue, expectedIsVerified, expectedFromDateString, expectedToDateString);

        JSONArray jsonLines = addressValue.getJSONArray("lines");
        Assertions.assertThat(jsonLines.toList()).isEqualTo(lines);
        if(postcode.isPresent()) {
            Assertions.assertThat(addressValue.getString("postCode")).isEqualTo(postcode.get());
        } else {
            Assertions.assertThat(addressValue.has("postCode")).isFalse();
        }
        if(internationalPostcode.isPresent()) {
            Assertions.assertThat(addressValue.getString("internationalPostCode")).isEqualTo(internationalPostcode.get());
        } else {
            Assertions.assertThat(addressValue.has("internationalPostCode")).isFalse();
        }
        if(uprn.isPresent()) {
            Assertions.assertThat(addressValue.getString("uprn")).isEqualTo(uprn.get());
        } else {
            Assertions.assertThat(addressValue.has("uprn")).isFalse();
        }
    }

    public static void checkMdsValueInArrayAttribute(
            String attributeName,
            int index,
            String expectedValue,
            boolean expectedIsVerified,
            String expectedFromDateString,
            String expectedToDateString,
            JSONObject attributes
    ) {
        JSONArray jsonArray = attributes.getJSONArray(attributeName);
        JSONObject mdsObjectJson = jsonArray.getJSONObject(index);
        checkMdsValueInJsonObject(mdsObjectJson, expectedValue, expectedIsVerified, expectedFromDateString, expectedToDateString);
    }

    public static void checkMdsValueInArrayAttributeWithoutDates(
            String attributeName,
            int index,
            String expectedValue,
            boolean expectedIsVerified,
            JSONObject attributes
    ) {
        JSONArray jsonArray = attributes.getJSONArray(attributeName);
        JSONObject mdsObjectJson = jsonArray.getJSONObject(index);
        checkMdsValueInJsonObject(mdsObjectJson, expectedValue, expectedIsVerified);
    }

    public static void checkMdsValueInJsonObject(
            JSONObject jsonObject,
            String expectedValue,
            boolean expectedIsVerified,
            String expectedFromDateString,
            String expectedToDateString
    ) {
        Assertions.assertThat(jsonObject.getString("value")).isEqualTo(expectedValue);
        checkMdsMetadataInJsonObject(jsonObject, expectedIsVerified, expectedFromDateString, expectedToDateString);
    }

    public static void checkMdsMetadataInJsonObject(
            JSONObject jsonObject,
            boolean expectedIsVerified,
            String expectedFromDateString,
            String expectedToDateString
    ) {
        Assertions.assertThat(jsonObject.getBoolean("verified")).isEqualTo(expectedIsVerified);
        Assertions.assertThat(jsonObject.getString("from")).isEqualTo(expectedFromDateString);
        if (expectedToDateString != null) {
            Assertions.assertThat(jsonObject.getString("to")).isEqualTo(expectedToDateString);
        }
    }

    public static void checkMdsValueInJsonObject(
            JSONObject jsonObject,
            String expectedValue,
            boolean expectedIsVerified
    ) {
        Assertions.assertThat(jsonObject.getString("value")).isEqualTo(expectedValue);
        checkMdsMetadataInJsonObject(jsonObject, expectedIsVerified);
    }

    public static void checkMdsMetadataInJsonObject(
            JSONObject jsonObject,
            boolean expectedIsVerified
    ) {
        Assertions.assertThat(jsonObject.getBoolean("verified")).isEqualTo(expectedIsVerified);
        Assertions.assertThat(jsonObject.has("from")).isFalse();
        Assertions.assertThat(jsonObject.has("to")).isFalse();
    }
}
