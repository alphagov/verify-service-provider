package uk.gov.ida.verifyserviceprovider.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.X509Certificate;
import uk.gov.ida.saml.security.PublicKeyFactory;

import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Stream;

public class MetadataPublicKeyExtractor {

    private final String entityId;
    private final MetadataResolver metadataResolver;
    private PublicKeyFactory publicKeyFactory;

    public MetadataPublicKeyExtractor(
        String entityId,
        MetadataResolver metadataResolver,
        PublicKeyFactory publicKeyFactory
    ) {
        this.entityId = entityId;
        this.metadataResolver = metadataResolver;
        this.publicKeyFactory = publicKeyFactory;
    }

    public PublicKey getEncryptionPublicKey() {
        try {
            CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(entityId));
            return Optional.ofNullable(metadataResolver.resolveSingle(criteria))
                .flatMap(this::getPublicKeys)
                .orElseThrow(this::missingEntityIdException);
        } catch (ResolverException e) {
            throw new RuntimeException(e);
        }
    }

    private RuntimeException missingEntityIdException() {
        return new RuntimeException(MessageFormat.format(
            "No public key for entity-id: \"{0}\" could be found in the metadata. Metadata could be expired, invalid, or missing entities",
            entityId));
    }

    private Optional<PublicKey> getPublicKeys(EntityDescriptor entityDescriptor) {
        return entityDescriptor
            .getSPSSODescriptor(SAMLConstants.SAML20P_NS)
            .getKeyDescriptors()
            .stream()
            .filter(keyDescriptor -> keyDescriptor.getUse() == UsageType.ENCRYPTION)
            .flatMap(this::getCertificateFromKeyDescriptor)
            .map(publicKeyFactory::create)
            .findFirst();
    }

    private Stream<X509Certificate> getCertificateFromKeyDescriptor(KeyDescriptor keyDescriptor) {
        return keyDescriptor.getKeyInfo()
            .getX509Datas()
            .stream()
            .flatMap(x509Data -> x509Data.getX509Certificates().stream());
    }
}
