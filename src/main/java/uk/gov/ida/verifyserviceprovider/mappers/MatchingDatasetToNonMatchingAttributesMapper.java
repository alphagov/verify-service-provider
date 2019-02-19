package uk.gov.ida.verifyserviceprovider.mappers;

import uk.gov.ida.saml.core.domain.Address;
import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAddress;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingAttributes;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingVerifiableAttribute;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetToNonMatchingAttributesMapper {

    public NonMatchingAttributes mapToNonMatchingAttributes(MatchingDataset matchingDataset) {
        List<NonMatchingVerifiableAttribute<String>> firstNames = matchingDataset.getFirstNames().stream()
                .map(this::mapToNonMatchingVerifiableAttribute)
                .collect(Collectors.toList());
        List<NonMatchingVerifiableAttribute<LocalDate>> datesOfBirth = matchingDataset.getDateOfBirths().stream()
            .map(MatchingDatasetToNonMatchingAttributesMapper::convertWrappedJodaLocalDateToJavaLocalDate)
            .map(this::mapToNonMatchingVerifiableAttribute)
            .collect(Collectors.toList());
        List<NonMatchingVerifiableAttribute<String>> middleNames = matchingDataset.getMiddleNames()
                .stream().map(this::mapToNonMatchingVerifiableAttribute)
                .collect(Collectors.toList());
        List<NonMatchingVerifiableAttribute<String>> surnames = matchingDataset.getSurnames().stream()
                .map(this::mapToNonMatchingVerifiableAttribute)
                .collect(Collectors.toList());
        NonMatchingVerifiableAttribute<Gender> gender = matchingDataset.getGender()
                .map(this::mapToNonMatchingVerifiableAttribute)
                .orElse(null);
        List<NonMatchingVerifiableAttribute<NonMatchingAddress>> addresses = mapAddresses(matchingDataset.getAddresses());

        return new NonMatchingAttributes(
            firstNames,
            middleNames,
            surnames,
            datesOfBirth,
            gender,
            addresses
        );
    }

    private <T> NonMatchingVerifiableAttribute<T> mapToNonMatchingVerifiableAttribute(SimpleMdsValue<T> simpleMdsValueOptional) {
        LocalDateTime from = Optional.ofNullable(simpleMdsValueOptional.getFrom())
                .map(MatchingDatasetToNonMatchingAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                .orElse(null);

        LocalDateTime to = Optional.ofNullable(simpleMdsValueOptional.getTo())
                .map(MatchingDatasetToNonMatchingAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                .orElse(null);

        return new NonMatchingVerifiableAttribute<>(
            simpleMdsValueOptional.getValue(),
            simpleMdsValueOptional.isVerified(),
            from,
            to
        );
    }

    private List<NonMatchingVerifiableAttribute<NonMatchingAddress>> mapAddresses(List<Address> addresses) {
        List<NonMatchingVerifiableAttribute<NonMatchingAddress>> output = new java.util.ArrayList<>();

        for (Address input : addresses) {
            NonMatchingAddress transformedAddress = new NonMatchingAddress(
                input.getLines(),
                input.getPostCode().orElse(""),
                input.getInternationalPostCode().orElse("")
            );

            LocalDateTime from = Optional.ofNullable(input.getFrom())
                    .map(MatchingDatasetToNonMatchingAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                    .orElse(null);

            LocalDateTime to = input.getTo()
                    .map(MatchingDatasetToNonMatchingAttributesMapper::convertJodaDateTimeToJavaLocalDateTime)
                    .orElse(null);

            NonMatchingVerifiableAttribute<NonMatchingAddress> addressAttribute = new NonMatchingVerifiableAttribute<>(
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
