package uk.gov.ida.verifyserviceprovider.metadata;

import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.impl.EntitiesDescriptorBuilder;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.KeyName;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.saml.serializers.XmlObjectToElementTransformer;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.CouldNotSignMetadataException;
import uk.gov.ida.verifyserviceprovider.exceptions.EncryptionCertDoesNotMatchPrivateKeyException;
import uk.gov.ida.verifyserviceprovider.exceptions.SigningCertDoesNotMatchPrivateKeyException;

import java.security.KeyException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MetadataRepository {

    private final VerifyServiceProviderConfiguration verifyServiceProviderConfiguration;
    private final Function<EntitiesDescriptor, Element> entitiesDescriptorElementTransformer;
    private final X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
    private final List<String> verifyServiceProviderEntityIds;
    private final XMLObjectBuilderFactory openSamlBuilderFactory;
    private final SignatureFactory signatureFactory;

    public MetadataRepository(VerifyServiceProviderConfiguration verifyServiceProviderConfiguration) {

        this.verifyServiceProviderConfiguration = verifyServiceProviderConfiguration;
        this.verifyServiceProviderEntityIds = verifyServiceProviderConfiguration.getServiceEntityIds();
        this.openSamlBuilderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        try {
            if(!verifyServiceProviderConfiguration.getSamlPrimarySigningCert().getPublicKey()
                    .equals(KeySupport.derivePublicKey(verifyServiceProviderConfiguration.getSamlSigningKey()))) {
                throw new SigningCertDoesNotMatchPrivateKeyException();
            }
        } catch (KeyException e) {
            throw new SigningCertDoesNotMatchPrivateKeyException();
        }
        try {
            if(!verifyServiceProviderConfiguration.getSamlPrimaryEncryptionCert().getPublicKey()
                    .equals(KeySupport.derivePublicKey(verifyServiceProviderConfiguration.getSamlPrimaryEncryptionKey()))) {
                throw new EncryptionCertDoesNotMatchPrivateKeyException();
            }
        } catch (KeyException e) {
            throw new EncryptionCertDoesNotMatchPrivateKeyException();
        }
        this.signatureFactory = getSignatureFactory(verifyServiceProviderConfiguration.getSamlPrimarySigningCert().getCert(), new KeyPair(verifyServiceProviderConfiguration.getSamlPrimarySigningCert().getPublicKey(),verifyServiceProviderConfiguration.getSamlSigningKey()));
        this.entitiesDescriptorElementTransformer = getEntityDescriptorToElementTransformer();
    }

    private SignatureFactory getSignatureFactory(String signingCert, KeyPair signingKeyPair) {
        IdaKeyStore keyStore = new IdaKeyStore(x509CertificateFactory.createCertificate(signingCert), signingKeyPair, Collections.emptyList());
        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        DigestSHA256 digestAlgorithm = new DigestSHA256();
        SignatureFactory signatureFactory = new SignatureFactory(true, keyStoreCredentialRetriever, signatureAlgorithm, digestAlgorithm);
        return signatureFactory;
    }

    private Function<EntitiesDescriptor, Element> getEntityDescriptorToElementTransformer() {
        return new XmlObjectToElementTransformer<>();
    }

    private EntityDescriptor createEntityDescriptor(String entityId) {
        XMLObjectBuilderFactory openSamlBuilderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        EntityDescriptor entityDescriptor = (EntityDescriptor) openSamlBuilderFactory.getBuilder(EntityDescriptor.TYPE_NAME).buildObject(EntityDescriptor.DEFAULT_ELEMENT_NAME, EntityDescriptor.TYPE_NAME);
        entityDescriptor.setEntityID(entityId);
        return entityDescriptor;
    }

    public Document getVerifyServiceProviderMetadata() {
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorBuilder().buildObject();
        entitiesDescriptor.setValidUntil(DateTime.now().plusHours(1));

        for(String entityId : verifyServiceProviderEntityIds) {
            final EntityDescriptor vspEntityDescriptor = createEntityDescriptor(entityId);
            vspEntityDescriptor.getRoleDescriptors().add(getSpSsoDescriptor(entityId));
            entitiesDescriptor.getEntityDescriptors().add(vspEntityDescriptor);
        }

        sign(entitiesDescriptor);

        return entitiesDescriptorElementTransformer.apply(entitiesDescriptor).getOwnerDocument();
    }

    private RoleDescriptor getSpSsoDescriptor(String entityId) {
        SPSSODescriptor spssoDescriptor = createSPSSODescriptor();
        spssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

        spssoDescriptor.getKeyDescriptors().addAll(getKeyDescriptors(entityId));

        return spssoDescriptor;
    }

    private Collection<? extends KeyDescriptor> getKeyDescriptors(String entityId) {
        Collection<Certificate> certificates = new ArrayList<>();
        certificates.add(getCertificate(entityId, verifyServiceProviderConfiguration.getSamlPrimarySigningCert(), Certificate.KeyUse.Signing));
        if(Objects.nonNull(verifyServiceProviderConfiguration.getSamlSecondarySigningCert())) {
            certificates.add(getCertificate(entityId, verifyServiceProviderConfiguration.getSamlSecondarySigningCert(), Certificate.KeyUse.Signing));
        }
        certificates.add(getCertificate(entityId, verifyServiceProviderConfiguration.getSamlPrimaryEncryptionCert(), Certificate.KeyUse.Encryption));
        return fromCertificates(certificates);
    }

    private Certificate getCertificate(String entityId, DeserializablePublicKeyConfiguration samlPrimarySigningCert, Certificate.KeyUse signing) {
        return new Certificate(entityId,
                samlPrimarySigningCert.getCert()
                        .replaceAll("-----BEGIN CERTIFICATE-----\n", "")
                        .replaceAll("-----END CERTIFICATE-----", ""),
                signing);
    }


    private void sign(EntitiesDescriptor entitiesDescriptor) {
        entitiesDescriptor.setSignature(signatureFactory.createSignature());
        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(entitiesDescriptor).marshall(entitiesDescriptor);
        } catch (MarshallingException e) {
            throw new CouldNotSignMetadataException(e);
        }
        try {
            Signer.signObject(entitiesDescriptor.getSignature());
        } catch (SignatureException e) {
            throw new CouldNotSignMetadataException(e);
        }
    }

    public SPSSODescriptor createSPSSODescriptor() {
        return (SPSSODescriptor) openSamlBuilderFactory.getBuilder(SPSSODescriptor.DEFAULT_ELEMENT_NAME).buildObject(SPSSODescriptor.DEFAULT_ELEMENT_NAME, SPSSODescriptor.TYPE_NAME);
    }

    public List<KeyDescriptor> fromCertificates(Collection<Certificate> certificateDtos) {
        List<KeyDescriptor> keyDescriptors = new ArrayList<>();
        for (Certificate certificateDto : certificateDtos) {
            KeyDescriptor keyDescriptor = createKeyDescriptor(certificateDto.getKeyUse().toString());
            KeyInfo keyInfo = createKeyInfo(certificateDto.getIssuerId());
            keyDescriptor.setKeyInfo(keyInfo);
            X509Data x509Data = createX509Data();
            final X509Certificate x509Certificate = createX509Certificate(certificateDto.getCertificate());
            x509Data.getX509Certificates().add(x509Certificate);
            keyInfo.getX509Datas().add(x509Data);
            keyDescriptors.add(keyDescriptor);
        }
        return keyDescriptors;
    }

    public KeyDescriptor createKeyDescriptor(String use) {
        KeyDescriptor keyDescriptor = (KeyDescriptor) openSamlBuilderFactory.getBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME).buildObject(KeyDescriptor.DEFAULT_ELEMENT_NAME, KeyDescriptor.TYPE_NAME);
        keyDescriptor.setUse(UsageType.valueOf(use.toUpperCase()));
        return keyDescriptor;
    }


    public KeyInfo createKeyInfo(String keyNameValue) {
        final KeyInfo keyInfo = (KeyInfo) openSamlBuilderFactory.getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME).buildObject(KeyInfo.DEFAULT_ELEMENT_NAME, KeyInfo.TYPE_NAME);
        if (keyNameValue != null) {
            KeyName keyName = createKeyName(keyNameValue);
            keyInfo.getKeyNames().add(keyName);
        }
        return keyInfo;
    }

    private KeyName createKeyName(String keyNameValue) {
        final KeyName keyName = (KeyName) openSamlBuilderFactory.getBuilder(KeyName.DEFAULT_ELEMENT_NAME).buildObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(keyNameValue);
        return keyName;
    }

    public X509Data createX509Data() {
        return (X509Data) openSamlBuilderFactory.getBuilder(X509Data.DEFAULT_ELEMENT_NAME).buildObject(X509Data.DEFAULT_ELEMENT_NAME, X509Data.TYPE_NAME);
    }

    public X509Certificate createX509Certificate(String cert) {
        X509Certificate x509Certificate = (X509Certificate) openSamlBuilderFactory.getBuilder(X509Certificate.DEFAULT_ELEMENT_NAME).buildObject(X509Certificate.DEFAULT_ELEMENT_NAME);
        x509Certificate.setValue(cert);
        return x509Certificate;
    }
}
