package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.extensions.Verified;
import uk.gov.ida.saml.core.extensions.impl.AddressImpl;
import uk.gov.ida.verifyserviceprovider.dto.AddressV1;
import uk.gov.ida.verifyserviceprovider.dto.AttributesV1;
import uk.gov.ida.verifyserviceprovider.dto.VerifiableAttributeV1;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class AttributeTranslationService {

    public static AttributesV1 translateAttributes(AttributeStatement attributeStatement) {
        List<Attribute> statementAttributes = attributeStatement.getAttributes();

        VerifiableAttributeV1<String> verifiableFirstName = getVerifiableStringAttribute(statementAttributes, "firstname", "firstname_verified");
        VerifiableAttributeV1<String> verifiableMiddleName = getVerifiableStringAttribute(statementAttributes, "middlename", "middlename_verified");
        VerifiableAttributeV1<String> verifiableSurname = getVerifiableStringAttribute(statementAttributes, "surname", "surname_verified");
        VerifiableAttributeV1<LocalDate> verifiableDob = getVerifiableDateAttribute(statementAttributes, "dateofbirth", "dateofbirth_verified");
        VerifiableAttributeV1<AddressV1> verifiableAddress = getVerifiableAddressAttribute(statementAttributes, "currentaddress", "currentaddress_verified");
        Optional<List<VerifiableAttributeV1<AddressV1>>> addressHistory = getVerifiableAddressListAttribute(statementAttributes, "addresshistory");
        Optional<String> cycle3 = getStringAttributeValue(statementAttributes, "cycle_3");
        return new AttributesV1(verifiableFirstName, verifiableMiddleName, verifiableSurname, verifiableDob, verifiableAddress, addressHistory.orElse(null), cycle3.orElse(null));
    }

    private static VerifiableAttributeV1<String> getVerifiableStringAttribute(List<Attribute> statementAttributes, String attributeName, String attributeVerifiedName) {
        final Optional<String> attributeValue = getStringAttributeValue(statementAttributes, attributeName);
        final Optional<Boolean> attributeVerified = getBooleanAttributeValue(statementAttributes, attributeVerifiedName);
        return VerifiableAttributeV1.fromOptionals(attributeValue, attributeVerified);
    }

    private static VerifiableAttributeV1<LocalDate> getVerifiableDateAttribute(List<Attribute> statementAttributes, String attributeName, String attributeVerifiedName) {
        final Optional<LocalDate> attributeValue = getDateAttributeValue(statementAttributes, attributeName);
        final Optional<Boolean> attributeVerified = getBooleanAttributeValue(statementAttributes, attributeVerifiedName);
        return VerifiableAttributeV1.fromOptionals(attributeValue, attributeVerified);
    }

    private static VerifiableAttributeV1<AddressV1> getVerifiableAddressAttribute(List<Attribute> statementAttributes, String attributeName, String attributeVerifiedName) {
        final Optional<AddressV1> attributeValue = getAddressAttributeValue(statementAttributes, attributeName);
        final Optional<Boolean> attributeVerified = getBooleanAttributeValue(statementAttributes, attributeVerifiedName);
        return VerifiableAttributeV1.fromOptionals(attributeValue, attributeVerified);
    }

    private static Optional<List<VerifiableAttributeV1<AddressV1>>> getVerifiableAddressListAttribute(List<Attribute> statementAttributes, String attributeName) {
        final Optional<Attribute> attribute = getAttribute(statementAttributes, attributeName);
        return attribute.map(
            attr -> attr.getAttributeValues().stream().map(
                val -> toVerifiableAddress((AddressImpl) val)
            ).collect(Collectors.toList())
        );
    }

    private static Optional<Attribute> getAttribute(List<Attribute> attributes, String name) {
        return attributes.stream().filter(a -> a.getName().equals(name)).findFirst();
    }

    private static Optional<String> getStringAttributeValue(List<Attribute> attributes, String attributeName) {
        final Optional<Attribute> attribute = getAttribute(attributes, attributeName);
        return attribute.map(attr -> {
            StringValueSamlObject attributeValue = ((StringValueSamlObject) attr.getAttributeValues().get(0));
            return Optional.ofNullable(attributeValue.getValue()).orElse("");
        });
    }

    private static Optional<Boolean> getBooleanAttributeValue(List<Attribute> attributes, String attributeName) {
        final Optional<Attribute> attribute = getAttribute(attributes, attributeName);
        return attribute.map(attr -> ((Verified) attr.getAttributeValues().get(0)).getValue());
    }

    private static Optional<LocalDate> getDateAttributeValue(List<Attribute> attributes, String attributeName) {
        return getStringAttributeValue(attributes, attributeName).map(x -> {
            try {
                return LocalDate.parse(x, DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException e) {
                throw new SamlResponseValidationException(
                    String.format("Error in SAML date format for attribute '%s'. Expected ISO date format, got: '%s'",
                        attributeName,
                        e.getParsedString())
                );
            }
        });
    }

    private static Optional<AddressV1> getAddressAttributeValue(List<Attribute> attributes, String attributeName) {
        final Optional<Attribute> attribute = getAttribute(attributes, attributeName);
        return attribute.map(attr -> toAddress((AddressImpl) attr.getAttributeValues().get(0)));
    }

    private static AddressV1 toAddress(AddressImpl address) {
        return new AddressV1(
            address.getLines().stream().map(StringValueSamlObject::getValue).collect(toList()),
            getValueOrNull(address.getPostCode()),
            getValueOrNull(address.getInternationalPostCode()),
            getValueOrNull(address.getUPRN()),
            convertToJavaLocalDate(address.getFrom()),
            convertToJavaLocalDate(address.getTo())
        );
    }

    private static VerifiableAttributeV1<AddressV1> toVerifiableAddress(AddressImpl address) {
        return new VerifiableAttributeV1<>(toAddress(address), address.getVerified());
    }

    private static String getValueOrNull(StringValueSamlObject attributeValue) {
        return Optional.ofNullable(attributeValue).map(StringValueSamlObject::getValue).orElse(null);
    }

    private static LocalDate convertToJavaLocalDate(org.joda.time.DateTime joda) {
        if (joda == null) {
            return null;
        }

        return LocalDate.of(joda.getYear(), joda.getMonthOfYear(), joda.getDayOfMonth());
    }
}
