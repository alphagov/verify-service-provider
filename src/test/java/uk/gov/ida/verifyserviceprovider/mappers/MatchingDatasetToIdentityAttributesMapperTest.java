package uk.gov.ida.verifyserviceprovider.mappers;

import org.joda.time.DateTime;
import org.junit.Test;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;
import uk.gov.ida.verifyserviceprovider.dto.Address;
import uk.gov.ida.verifyserviceprovider.dto.IdentityAttributes;
import uk.gov.ida.verifyserviceprovider.dto.TransliterableAttribute;
import uk.gov.ida.verifyserviceprovider.dto.HistoricalVerifiableAttribute;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingVerifiableAttributeBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class MatchingDatasetToIdentityAttributesMapperTest {

    private final org.joda.time.DateTime fromTwo = org.joda.time.DateTime.now().minusDays(30);
    private final org.joda.time.DateTime fromOne = org.joda.time.DateTime.now();
    private final org.joda.time.DateTime fromThree = org.joda.time.DateTime.now().minusDays(6);
    private final org.joda.time.DateTime fromFour = null;

    private final String foo = "Foo";
    private final String bar = "Bar";
    private final String baz = "Baz";
    private final String fuu = "Fuu";

    @Test
    public void shouldMapFirstNames() {
        List<TransliterableMdsValue> firstNames = asList(
                createTransliterableValue(fromThree, foo),
                createTransliterableValue(fromTwo, bar),
                createTransliterableValue(fromOne, baz),
                createTransliterableValue(fromFour, fuu)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                firstNames,
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        assertThat(identityAttributes.getFirstNames().stream()
                .map(HistoricalVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(identityAttributes.getFirstNames()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapFirstNamesWithNonLatinScriptValue() {
        String nonLatinScript = "nonLatinScript";
        List<TransliterableMdsValue> firstNames = asList(
                createTransliterableValue(foo, nonLatinScript)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                firstNames,
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        TransliterableAttribute transliterableAttribute = identityAttributes.getFirstNames().get(0);
        assertThat(transliterableAttribute.getValue()).isEqualTo(foo);
        assertThat(transliterableAttribute.getNonLatinScriptValue()).isEqualTo(nonLatinScript);
    }

    @Test
    public void shouldMapMiddlenames() {
        List<SimpleMdsValue<String>> middleNames = asList(
                createSimpleMdsValue(fromThree, foo),
                createSimpleMdsValue(fromTwo, bar),
                createSimpleMdsValue(fromOne, baz),
                createSimpleMdsValue(fromFour, fuu)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                middleNames,
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        assertThat(identityAttributes.getMiddleNames().stream()
                .map(HistoricalVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(identityAttributes.getMiddleNames()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapSurnames() {
        List<TransliterableMdsValue> surnames = asList(
                createTransliterableValue(fromThree, foo),
                createTransliterableValue(fromTwo, bar),
                createTransliterableValue(fromOne, baz),
                createTransliterableValue(fromFour, fuu)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                surnames,
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        assertThat(identityAttributes.getSurnames().stream()
                .map(HistoricalVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(identityAttributes.getSurnames()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapSurnamesWithNonLatinScriptValue() {
        String nonLatinScript = "nonLatinScript";
        List<TransliterableMdsValue> surnames = asList(
                createTransliterableValue(foo, nonLatinScript)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                surnames,
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        TransliterableAttribute transliterableAttribute = identityAttributes.getSurnames().get(0);
        assertThat(transliterableAttribute.getValue()).isEqualTo(foo);
        assertThat(transliterableAttribute.getNonLatinScriptValue()).isEqualTo(nonLatinScript);
    }

    @Test
    public void shouldMapDatesOfBirth() {
        org.joda.time.LocalDate fooDate = org.joda.time.LocalDate.now();
        org.joda.time.LocalDate barDate = org.joda.time.LocalDate.now().minusDays(5);
        org.joda.time.LocalDate bazDate = org.joda.time.LocalDate.now().minusDays(10);
        org.joda.time.LocalDate fuuDate = org.joda.time.LocalDate.now().minusDays(15);
        List<SimpleMdsValue<org.joda.time.LocalDate>> datesOfBirth = asList(
                createDateValue(fromThree, fooDate),
                createDateValue(fromTwo, barDate),
                createDateValue(fromOne, bazDate),
                createDateValue(fromFour, fuuDate)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                datesOfBirth,
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        List<String> expectedDates = Stream.of(bazDate, fooDate, barDate, fuuDate)
                .map(org.joda.time.LocalDate::toString)
                .collect(Collectors.toList());
        assertThat(identityAttributes.getDatesOfBirth().stream()
                .map(HistoricalVerifiableAttribute::getValue)
                .map(java.time.LocalDate::toString)
                .collect(Collectors.toList()))
                .isEqualTo(expectedDates);
        assertThat(identityAttributes.getDatesOfBirth()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapAddressesAndNotDiscardAttributes() {
        uk.gov.ida.saml.core.domain.Address addressIn = createAddress(fromOne, baz);

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.singletonList(addressIn),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        Address addressOut = identityAttributes.getAddresses().get(0).getValue();
        assertThat(addressOut.getPostCode()).isEqualTo(addressIn.getPostCode().get());
        assertThat(addressOut.getInternationalPostCode()).isEqualTo(addressIn.getInternationalPostCode().get());
        assertThat(addressOut.getUprn()).isEqualTo(addressIn.getUPRN().get());
        assertThat(addressOut.getLines()).isEqualTo(addressIn.getLines());

        assertThat(identityAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapCurrentAddress() {
        List<uk.gov.ida.saml.core.domain.Address> currentAddress = asList(
                createAddress(fromThree, foo),
                createAddress(fromTwo, bar),
                createAddress(fromOne, baz),
                createAddress(fromFour, fuu)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                currentAddress,
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        assertThat(identityAttributes.getAddresses().stream()
                .map(HistoricalVerifiableAttribute::getValue)
                .map(Address::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(identityAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapPreviousAddress() {
        List<uk.gov.ida.saml.core.domain.Address> previousAddress = asList(
                createAddress(fromThree, foo),
                createAddress(fromTwo, bar),
                createAddress(fromOne, baz),
                createAddress(fromFour, fuu)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                previousAddress,
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        assertThat(identityAttributes.getAddresses().stream()
                .map(HistoricalVerifiableAttribute::getValue)
                .map(Address::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(identityAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapAndMergeAddress() {
        List<uk.gov.ida.saml.core.domain.Address> previousAddress = asList(
                createAddress(fromThree, foo),
                createAddress(fromFour, fuu)
        );
        List<uk.gov.ida.saml.core.domain.Address> currentAddress = asList(
                createAddress(fromOne, baz),
                createAddress(fromTwo, bar)
        );

        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                currentAddress,
                previousAddress,
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        assertThat(identityAttributes.getAddresses().stream()
                .map(HistoricalVerifiableAttribute::getValue)
                .map(Address::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(identityAttributes.getAddresses()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapGender() {
        Gender gender = Gender.NOT_SPECIFIED;
        MatchingDataset matchingDataset = new MatchingDataset(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.of(new SimpleMdsValue<>(gender, null, null, true)),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );
        IdentityAttributes identityAttributes = new MatchingDatasetToIdentityAttributesMapper().mapToIdentityAttributes(matchingDataset);

        assertThat(identityAttributes.getGender().getValue()).isEqualTo(gender);
    }

    @Test
    public void sortTheListByToDateThenIsVerifiedThenFromDate() {
        LocalDate now = LocalDate.now();
        LocalDate fiveDaysAgo = now.minusDays(5);
        LocalDate threeDaysAgo = now.minusDays(3);
        HistoricalVerifiableAttribute<String> attributeOne = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(null).withFrom(now).build();
        HistoricalVerifiableAttribute<String> attributeTwo = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(null).withFrom(fiveDaysAgo).build();
        HistoricalVerifiableAttribute<String> attributeThree = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(null).withFrom(now).build();
        HistoricalVerifiableAttribute<String> attributeFour = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(now).withFrom(now).build();
        HistoricalVerifiableAttribute<String> attributeFive = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(now).withFrom(fiveDaysAgo).build();
        HistoricalVerifiableAttribute<String> attributeSix = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(fiveDaysAgo).withFrom(now).build();
        HistoricalVerifiableAttribute<String> attributeSeven = new NonMatchingVerifiableAttributeBuilder().withVerified(true).withTo(fiveDaysAgo).withFrom(threeDaysAgo).build();
        HistoricalVerifiableAttribute<String> attributeEight = new NonMatchingVerifiableAttributeBuilder().withVerified(false).withTo(fiveDaysAgo).withFrom(null).build();
        List<HistoricalVerifiableAttribute<String>> unsorted = asList(
                attributeFour,
                attributeOne,
                attributeSix,
                attributeTwo,
                attributeSeven,
                attributeFive,
                attributeThree,
                attributeEight
        );
        assertThat(unsorted.stream().sorted(MatchingDatasetToIdentityAttributesMapper.attributeComparator()).collect(Collectors.toList())).isEqualTo(
                asList(
                        attributeOne,
                        attributeTwo,
                        attributeThree,
                        attributeFour,
                        attributeFive,
                        attributeSix,
                        attributeSeven,
                        attributeEight
                )
        );
    }

    private Comparator<HistoricalVerifiableAttribute<?>> comparedByFromDate() {
        return Comparator.comparing(HistoricalVerifiableAttribute::getFrom, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private uk.gov.ida.saml.core.domain.Address createAddress(DateTime from, String postCode) {
        return new uk.gov.ida.saml.core.domain.Address(Collections.emptyList(), postCode, "BAR", "BAZ", from, null, true);
    }

    private TransliterableMdsValue createTransliterableValue(DateTime from, String value) {
        return new TransliterableMdsValue(createSimpleMdsValue(from, value));
    }

    private TransliterableMdsValue createTransliterableValue(String value, String nonLatinScript) {
        return new TransliterableMdsValue(value, nonLatinScript);
    }

    private SimpleMdsValue<String> createSimpleMdsValue(DateTime from, String value) {
        return new SimpleMdsValue<>(value, from, null, true);
    }

    private SimpleMdsValue<org.joda.time.LocalDate> createDateValue(org.joda.time.DateTime from, org.joda.time.LocalDate dateTime) {
        return new SimpleMdsValue<>(dateTime, from, null, true);
    }
}