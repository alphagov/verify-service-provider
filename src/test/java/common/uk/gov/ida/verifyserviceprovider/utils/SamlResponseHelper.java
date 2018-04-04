package common.uk.gov.ida.verifyserviceprovider.utils;

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.saml.core.extensions.Verified;
import uk.gov.ida.saml.core.test.OpenSamlXmlObjectFactory;

public class SamlResponseHelper {

    public static Attribute createVerifiedAttribute(String name, boolean value) {
        Attribute attribute = new OpenSamlXmlObjectFactory().createAttribute();
        attribute.setName(name);

        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        Verified verifiedValue = (Verified) builderFactory.getBuilder(Verified.TYPE_NAME).buildObject(Verified.DEFAULT_ELEMENT_NAME, Verified.TYPE_NAME);
        verifiedValue.setValue(value);

        attribute.getAttributeValues().add(verifiedValue);

        return attribute;
    }
}
