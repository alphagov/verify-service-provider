package uk.gov.ida.verifyserviceprovider.saml.extensions;

import com.google.common.collect.ImmutableList;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.common.AbstractSAMLObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VersionImpl extends AbstractSAMLObject implements Version {

    public static final Marshaller MARSHALLER = new VersionMarshaller();
    public static final Unmarshaller UNMARSHALLER = new VersionUnMarshaller();
    private ApplicationVersion applicationVersion;

    public VersionImpl() {
        super(Version.NAMESPACE_URI, Version.DEFAULT_ELEMENT_LOCAL_NAME, Version.NAMESPACE_PREFIX);
        super.setSchemaType(Version.TYPE_NAME);
    }

    public VersionImpl(@Nullable String namespaceURI, @Nonnull String elementLocalName, @Nullable String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    @Override
    public ApplicationVersion getApplicationVersion() {
        return this.applicationVersion;
    }

    @Override
    public void setApplicationVersion(ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return Stream.of(applicationVersion).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
