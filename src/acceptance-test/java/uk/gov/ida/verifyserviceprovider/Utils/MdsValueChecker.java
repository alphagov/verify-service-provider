package uk.gov.ida.verifyserviceprovider.Utils;

import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

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

    public static void checkMdsValueOfAddress(
            int index,
            List<String> lines,
            String postcode,
            String internationalPostcode,
            boolean expectedIsVerified,
            String expectedFromDateString,
            String expectedToDateString,
            JSONObject attributes
    ) {
        JSONArray addresses = attributes.getJSONArray("addresses");
        JSONObject addressMdsValue = addresses.getJSONObject(index);
        JSONObject addressValue = addressMdsValue.getJSONObject("value");

        // TODO: The Compliance Tool isn't currently returning from/to dates for addresses.  Until that is fixed, the next line needs to be commented out.
        //checkMdsMetadataInJsonObject(addressMdsValue, expectedIsVerified, expectedFromDateString, expectedToDateString);

        JSONArray jsonLines = addressValue.getJSONArray("lines");
        Assertions.assertThat(jsonLines.length()).isEqualTo(lines.size());
        for (index = 0; index < lines.size(); index++) {
            Assertions.assertThat(jsonLines.getString(index)).isEqualTo(lines.get(index));
        }
        Assertions.assertThat(addressValue.getString("postCode")).isEqualTo(postcode);
        Assertions.assertThat(addressValue.getString("internationalPostCode")).isEqualTo(internationalPostcode);
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
}
