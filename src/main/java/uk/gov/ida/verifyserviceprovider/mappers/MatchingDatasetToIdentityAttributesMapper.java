package uk.gov.ida.verifyserviceprovider.mappers;

import uk.gov.ida.saml.core.domain.Gender;
import uk.gov.ida.saml.core.domain.MatchingDataset;
import uk.gov.ida.saml.core.domain.SimpleMdsValue;
import uk.gov.ida.saml.core.domain.TransliterableMdsValue;
import uk.gov.ida.verifyserviceprovider.dto.HistoricalVerifiableAttribute;
import uk.gov.ida.verifyserviceprovider.dto.Address;
import uk.gov.ida.verifyserviceprovider.dto.IdentityAttributes;
import uk.gov.ida.verifyserviceprovider.dto.TransliterableAttribute;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchingDatasetToIdentityAttributesMapper {

    public IdentityAttributes mapToIdentityAttributes(MatchingDataset matchingDataset) {
        List<TransliterableAttribute> firstNames = convertTransliterableNameAttributes(matchingDataset.getFirstNames());
        List<HistoricalVerifiableAttribute<LocalDate>> datesOfBirth = convertDateOfBirths(matchingDataset.getDateOfBirths());
        List<HistoricalVerifiableAttribute<String>> middleNames = convertNameAttributes(matchingDataset.getMiddleNames());
        List<TransliterableAttribute> surnames = convertTransliterableNameAttributes(matchingDataset.getSurnames());
        HistoricalVerifiableAttribute<Gender> gender = matchingDataset.getGender()
                .map(this::mapToIdentityVerifiableAttribute)
                .orElse(null);
        List<HistoricalVerifiableAttribute<Address>> addresses = mapAddresses(matchingDataset.getAddresses());

        return new IdentityAttributes(
            firstNames,
            middleNames,
            surnames,
            datesOfBirth,
            gender,
            addresses
        );
    }

    private List<HistoricalVerifiableAttribute<String>> convertNameAttributes(List<SimpleMdsValue<String>> values) {
        return values.stream()
                .map(this::mapToIdentityVerifiableAttribute)
                .sorted(attributeComparator())
                .collect(Collectors.toList());
    }

    private List<HistoricalVerifiableAttribute<LocalDate>> convertDateOfBirths(List<SimpleMdsValue<org.joda.time.LocalDate>> values) {
        return values.stream()
                .map(MatchingDatasetToIdentityAttributesMapper::convertWrappedJodaLocalDateToJavaLocalDate)
                .map(this::mapToIdentityVerifiableAttribute)
                .sorted(attributeComparator())
                .collect(Collectors.toList());
    }

    private List<TransliterableAttribute> convertTransliterableNameAttributes(List<TransliterableMdsValue> values) {
        return values.stream()
                .map(this::mapToTransliterableAttribute)
                .sorted(attributeComparator())
                .collect(Collectors.toList());
    }


    private TransliterableAttribute mapToTransliterableAttribute(TransliterableMdsValue transliterableMdsValue) {
        LocalDate from = Optional.ofNullable(transliterableMdsValue.getFrom())
                .map(MatchingDatasetToIdentityAttributesMapper::convertJodaDateTimeToJavaLocalDate)
                .orElse(null);

        LocalDate to = Optional.ofNullable(transliterableMdsValue.getTo())
                .map(MatchingDatasetToIdentityAttributesMapper::convertJodaDateTimeToJavaLocalDate)
                .orElse(null);

        return new TransliterableAttribute(
                transliterableMdsValue.getValue(),
                transliterableMdsValue.getNonLatinScriptValue(),
                transliterableMdsValue.isVerified(),
                from,
                to
        );
    }

    private <T> HistoricalVerifiableAttribute<T> mapToIdentityVerifiableAttribute(SimpleMdsValue<T> simpleMdsValueOptional) {
        LocalDate from = Optional.ofNullable(simpleMdsValueOptional.getFrom())
                .map(MatchingDatasetToIdentityAttributesMapper::convertJodaDateTimeToJavaLocalDate)
                .orElse(null);

        LocalDate to = Optional.ofNullable(simpleMdsValueOptional.getTo())
                .map(MatchingDatasetToIdentityAttributesMapper::convertJodaDateTimeToJavaLocalDate)
                .orElse(null);

        return new HistoricalVerifiableAttribute<>(
            simpleMdsValueOptional.getValue(),
            simpleMdsValueOptional.isVerified(),
            from,
            to
        );
    }

    private List<HistoricalVerifiableAttribute<Address>> mapAddresses(List<uk.gov.ida.saml.core.domain.Address> addresses) {
        return addresses.stream().map(this::mapAddress).sorted(attributeComparator()).collect(Collectors.toList());
    }

    private HistoricalVerifiableAttribute<Address> mapAddress(uk.gov.ida.saml.core.domain.Address input) {
        Address transformedAddress = new Address(
            input.getLines(),
            input.getPostCode().orElse(null),
            input.getInternationalPostCode().orElse(null),
            input.getUPRN().orElse(null)
        );

        LocalDate from = Optional.ofNullable(input.getFrom())
                .map(MatchingDatasetToIdentityAttributesMapper::convertJodaDateTimeToJavaLocalDate)
                .orElse(null);

        LocalDate to = input.getTo()
                .map(MatchingDatasetToIdentityAttributesMapper::convertJodaDateTimeToJavaLocalDate)
                .orElse(null);

        return new HistoricalVerifiableAttribute<>(
            transformedAddress,
            input.isVerified(),
            from,
            to
        );
    }

    private static LocalDate convertJodaDateTimeToJavaLocalDate(org.joda.time.DateTime jodaDateTime) {
        return LocalDate.of(
            jodaDateTime.getYear(),
            jodaDateTime.getMonthOfYear(),
            jodaDateTime.getDayOfMonth()
        );
    }

    private static LocalDate convertJodaLocalDateToJavaLocalDate(org.joda.time.LocalDate jodaDate) {
        return LocalDate.of(jodaDate.getYear(), jodaDate.getMonthOfYear(), jodaDate.getDayOfMonth());
    }

    private static SimpleMdsValue<LocalDate> convertWrappedJodaLocalDateToJavaLocalDate(SimpleMdsValue<org.joda.time.LocalDate> wrappedJodaDate) {
        LocalDate javaLocalDate = convertJodaLocalDateToJavaLocalDate(wrappedJodaDate.getValue());
        return new SimpleMdsValue<>(javaLocalDate, wrappedJodaDate.getFrom(), wrappedJodaDate.getTo(), wrappedJodaDate.isVerified());
    }

    static <T> Comparator<HistoricalVerifiableAttribute<T>> attributeComparator() {
        return Comparator.<HistoricalVerifiableAttribute<T>, LocalDate>comparing(HistoricalVerifiableAttribute::getTo, Comparator.nullsFirst(Comparator.reverseOrder()))
                .thenComparing(HistoricalVerifiableAttribute::isVerified, Comparator.reverseOrder())
                .thenComparing(HistoricalVerifiableAttribute::getFrom, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
