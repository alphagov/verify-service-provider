package uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.Lists;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.extensions.Verified;
import uk.gov.ida.saml.core.extensions.impl.AddressImpl;
import uk.gov.ida.verifyserviceprovider.dto.Address;
import uk.gov.ida.verifyserviceprovider.dto.Attributes;
import uk.gov.ida.verifyserviceprovider.dto.VerifiableAttribute;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class AttributeTranslationService {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ssa";

    public static Attributes translateAttributes(AttributeStatement attributeStatement) {
        List<Attribute> statementAttributes = attributeStatement.getAttributes();

        VerifiableAttribute<String> verifiableFirstName = getVerifiableStringAttribute(statementAttributes, "FIRST_NAME", "FIRST_NAME_VERIFIED");
        VerifiableAttribute<String> verifiableMiddleName = getVerifiableStringAttribute(statementAttributes, "MIDDLE_NAME", "MIDDLE_NAME_VERIFIED");
        VerifiableAttribute<String> verifiableSurname = getVerifiableStringAttribute(statementAttributes, "SURNAME", "SURNAME_VERIFIED");
        VerifiableAttribute<LocalDate> verifiableDob = getVerifiableDateAttribute(statementAttributes, "DATE_OF_BIRTH", "DATE_OF_BIRTH_VERIFIED");
        VerifiableAttribute<Address> verifiableAddress = getVerifiableAddressAttribute(statementAttributes, "CURRENT_ADDRESS", "CURRENT_ADDRESS_VERIFIED");
        Optional<String> cycle3 = getStringAttributeValue(statementAttributes, "CYCLE_3");
        return new Attributes(verifiableFirstName, verifiableMiddleName, verifiableSurname, verifiableDob, verifiableAddress, cycle3.orElse(null));
    }

    private static VerifiableAttribute<String> getVerifiableStringAttribute(List<Attribute> statementAttributes, String attributeName, String attributeVerifiedName) {
        final Optional<String> attributeValue = getStringAttributeValue(statementAttributes, attributeName);
        final Optional<Boolean> attributeVerified = getBooleanAttributeValue(statementAttributes, attributeVerifiedName);
        return VerifiableAttribute.fromOptionals(attributeValue, attributeVerified);
    }

    private static VerifiableAttribute<LocalDate> getVerifiableDateAttribute(List<Attribute> statementAttributes, String attributeName, String attributeVerifiedName) {
        final Optional<LocalDate> attributeValue = getDateAttributeValue(statementAttributes, attributeName);
        final Optional<Boolean> attributeVerified = getBooleanAttributeValue(statementAttributes, attributeVerifiedName);
        return VerifiableAttribute.fromOptionals(attributeValue, attributeVerified);
    }

    private static VerifiableAttribute<Address> getVerifiableAddressAttribute(List<Attribute> statementAttributes, String attributeName, String attributeVerifiedName) {
        final Optional<Address> attributeValue = getAddressAttributeValue(statementAttributes, attributeName);
        final Optional<Boolean> attributeVerified = getBooleanAttributeValue(statementAttributes, attributeVerifiedName);
        return VerifiableAttribute.fromOptionals(attributeValue, attributeVerified);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return getStringAttributeValue(attributes, attributeName).map(x -> {
            try {
                return LocalDate.parse(x, formatter);
            } catch (DateTimeParseException e) {
                throw new SamlResponseValidationException(
                    String.format("Error in SAML date format for attribute '%s'. Expected pattern: '%s', got: '%s'",
                        attributeName,
                        DATE_TIME_FORMAT,
                        e.getParsedString())
                );
            }
        });
    }

    private static Optional<Address> getAddressAttributeValue(List<Attribute> attributes, String attributeName) {
        final Optional<Attribute> attribute = getAttribute(attributes, attributeName);
        return attribute.map(attr -> toAddress((AddressImpl) attr.getAttributeValues().get(0)));
    }

    private static Address toAddress(AddressImpl address) {
        return new Address(
            Lists.transform(address.getLines(), StringValueSamlObject::getValue),
            getValueOrNull(address.getPostCode()),
            getValueOrNull(address.getInternationalPostCode()),
            getValueOrNull(address.getUPRN()),
            convertToJavaLocalDate(address.getFrom()),
            convertToJavaLocalDate(address.getTo())
        );
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
