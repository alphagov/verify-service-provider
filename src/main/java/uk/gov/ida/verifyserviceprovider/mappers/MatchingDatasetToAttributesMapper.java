package uk.gov.ida.verifyserviceprovider.mappers;

import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;
import uk.gov.ida.verifyserviceprovider.dto.AddressV2;
import uk.gov.ida.verifyserviceprovider.dto.AttributesV2;
import uk.gov.ida.verifyserviceprovider.dto.VerifiableAttributeV2;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetToAttributesMapper {

    public AttributesV2 mapToAttributesV2(MatchingDataset matchingDataset) {
        Optional<TransliterableMdsValue> firstNameValue = matchingDataset.getFirstNames().stream().findFirst();
        Optional<SimpleMdsValue<LocalDate>> birthDateValue = matchingDataset.getDateOfBirths().stream()
            .map(MatchingDatasetToAttributesMapper::convertWrappedJodaLocalDateToJavaLocalDate)
            .findFirst();

        VerifiableAttributeV2<String> firstName = firstNameValue.map(this::mapToVerifiableAttributeV2).orElse(null);
        List<VerifiableAttributeV2<String>> middleNames = matchingDataset.getMiddleNames().stream().map(this::mapToVerifiableAttributeV2).collect(Collectors.toList());
        List<VerifiableAttributeV2<String>> surnames = matchingDataset.getSurnames().stream().map(this::mapToVerifiableAttributeV2).collect(Collectors.toList());
        VerifiableAttributeV2<LocalDate> dateOfBirth = birthDateValue.map(this::mapToVerifiableAttributeV2).orElse(null);
        VerifiableAttributeV2<Gender> gender = matchingDataset.getGender().map(this::mapToVerifiableAttributeV2).orElse(null);
        List<VerifiableAttributeV2<AddressV2>> addresses = mapAddresses(matchingDataset.getAddresses());

        return new AttributesV2(
            firstName,
            middleNames,
            surnames,
            dateOfBirth,
            gender,
            addresses
        );
    }

    private <T> VerifiableAttributeV2<T> mapToVerifiableAttributeV2(SimpleMdsValue<T> simpleMdsValueOptional) {
        LocalDateTime from = Optional.ofNullable(simpleMdsValueOptional.getFrom())
                .map(MatchingDatasetToAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                .orElse(null);

        LocalDateTime to = Optional.ofNullable(simpleMdsValueOptional.getTo())
                .map(MatchingDatasetToAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                .orElse(null);

        return new VerifiableAttributeV2<>(
            simpleMdsValueOptional.getValue(),
            simpleMdsValueOptional.isVerified(),
            from,
            to
        );
    }

    private List<VerifiableAttributeV2<AddressV2>> mapAddresses(List<Address> addresses) {
        List<VerifiableAttributeV2<AddressV2>> output = new java.util.ArrayList<>();

        for (Address input : addresses) {
            AddressV2 transformedAddress = new AddressV2(
                input.getLines(),
                input.getPostCode().orElse(""),
                input.getInternationalPostCode().orElse("")
            );

            LocalDateTime from = Optional.ofNullable(input.getFrom())
                    .map(MatchingDatasetToAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                    .orElse(null);

            LocalDateTime to = input.getTo()
                    .map(MatchingDatasetToAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                    .orElse(null);

            VerifiableAttributeV2<AddressV2> addressAttribute = new VerifiableAttributeV2<>(
                transformedAddress,
                input.isVerified(),
                from,
                to
            );

            output.add(addressAttribute);
        }

        return output;
    }

    private static LocalDateTime convertJodaDateTimeToJavaLocalDateTime(org.joda.time.DateTime jodaDateTime) {
        return LocalDateTime.of(
            jodaDateTime.getYear(),
            jodaDateTime.getMonthOfYear(),
            jodaDateTime.getDayOfMonth(),
            jodaDateTime.getHourOfDay(),
            jodaDateTime.getMinuteOfHour(),
            jodaDateTime.getSecondOfMinute(),
            jodaDateTime.getMillisOfSecond()
        );
    }

    private static LocalDate convertJodaLocalDateToJavaLocalDate(org.joda.time.LocalDate jodaDate) {
        return LocalDate.of(jodaDate.getYear(), jodaDate.getMonthOfYear(), jodaDate.getDayOfMonth());
    }

    private static SimpleMdsValue<LocalDate> convertWrappedJodaLocalDateToJavaLocalDate(SimpleMdsValue<org.joda.time.LocalDate> wrappedJodaDate) {
        LocalDate javaLocalDate = convertJodaLocalDateToJavaLocalDate(wrappedJodaDate.getValue());
        return new SimpleMdsValue<>(javaLocalDate, wrappedJodaDate.getFrom(), wrappedJodaDate.getTo(), wrappedJodaDate.isVerified());
    }
}
