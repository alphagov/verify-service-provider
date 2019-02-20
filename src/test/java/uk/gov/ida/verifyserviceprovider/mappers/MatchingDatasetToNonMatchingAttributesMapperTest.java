package uk.gov.ida.verifyserviceprovider.mappers;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAddress;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingVerifiableAttribute;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class MatchingDatasetToNonMatchingAttributesMapperTest {

    private final DateTime fromTwo = DateTime.now().minusDays(30);
    private final DateTime fromOne = DateTime.now();
    private final DateTime fromThree = DateTime.now().minusDays(6);
    private final DateTime fromFour = null;
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getFirstNames().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getFirstNames()).isSortedAccordingTo(comparedByFromDate());
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getMiddleNames().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getFirstNames()).isSortedAccordingTo(comparedByFromDate());
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getSurnames().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getSurnames()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapDatesOfBirth() {
        LocalDate fooDate = LocalDate.now();
        LocalDate barDate = LocalDate.now().minusDays(5);
        LocalDate bazDate = LocalDate.now().minusDays(10);
        LocalDate fuuDate = LocalDate.now().minusDays(15);
        List<SimpleMdsValue<LocalDate>> datesOfBirth = asList(
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        List<String> expectedDates = Stream.of(bazDate, fooDate, barDate, fuuDate)
                .map(LocalDate::toString)
                .collect(Collectors.toList());
        assertThat(nonMatchingAttributes.getDatesOfBirth().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(java.time.LocalDate::toString)
                .collect(Collectors.toList()))
                .isEqualTo(expectedDates);
        assertThat(nonMatchingAttributes.getDatesOfBirth()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapCurrentAddress() {
        List<Address> currentAddress = asList(
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getAddresses().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(NonMatchingAddress::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getDatesOfBirth()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapPreviousAddress() {
        List<Address> previousAddress = asList(
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getAddresses().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(NonMatchingAddress::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getDatesOfBirth()).isSortedAccordingTo(comparedByFromDate());
    }

    @Test
    public void shouldMapAndMergeAddress() {
        List<Address> previousAddress = asList(
                createAddress(fromThree, foo),
                createAddress(fromFour, fuu)
        );
        List<Address> currentAddress = asList(
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getAddresses().stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .map(NonMatchingAddress::getPostCode)
                .collect(Collectors.toList()))
                .isEqualTo(asList(baz, foo, bar, fuu));
        assertThat(nonMatchingAttributes.getDatesOfBirth()).isSortedAccordingTo(comparedByFromDate());
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
        NonMatchingAttributes nonMatchingAttributes = new MatchingDatasetToNonMatchingAttributesMapper().mapToNonMatchingAttributes(matchingDataset);

        assertThat(nonMatchingAttributes.getGender().getValue()).isEqualTo(gender);
    }

    private Comparator<NonMatchingVerifiableAttribute<?>> comparedByFromDate() {
        return Comparator.comparing(NonMatchingVerifiableAttribute::getFrom, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private Address createAddress(DateTime from, String postCode) {
        return new Address(Collections.emptyList(), postCode, null, null, from, null, true);
    }

    private TransliterableMdsValue createTransliterableValue(DateTime from, String value) {
        return new TransliterableMdsValue(createSimpleMdsValue(from, value));
    }

    private SimpleMdsValue<String> createSimpleMdsValue(DateTime from, String value) {
        return new SimpleMdsValue<>(value, from, null, true);
    }

    private SimpleMdsValue<LocalDate> createDateValue(DateTime from, LocalDate dateTime) {
        return new SimpleMdsValue<>(dateTime, from, null, true);
    }
}