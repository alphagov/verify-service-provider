package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLRuntimeException;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAttribute;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.ExtensionsBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.saml.core.extensions.versioning.Version;
import uk.gov.ida.saml.core.extensions.versioning.VersionImpl;
import uk.gov.ida.saml.core.extensions.versioning.application.ApplicationVersion;
import uk.gov.ida.saml.core.extensions.versioning.application.ApplicationVersionImpl;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.shared.utils.manifest.ManifestReader;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.factories.EncrypterFactory;

import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.util.Collections;
import java.util.UUID;

public class AuthnRequestFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AuthnRequestFactory.class);

    private final URI destination;
    private final ManifestReader manifestReader;
    private final EncrypterFactory encrypterFactory;
    private KeyPair signingKeyPair;

    public AuthnRequestFactory(
            URI destination,
            KeyPair signingKeyPair,
            ManifestReader manifestReader,
            EncrypterFactory encrypterFactory
    ) {
        this.destination = destination;
        this.manifestReader = manifestReader;
        this.encrypterFactory = encrypterFactory;
        this.signingKeyPair = signingKeyPair;
    }

    public AuthnRequest build(String serviceEntityId) {
        AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setID(String.format("_%s", UUID.randomUUID()));
        authnRequest.setIssueInstant(DateTime.now());
        authnRequest.setForceAuthn(false);
        authnRequest.setDestination(destination.toString());
        authnRequest.setExtensions(createExtensions());

        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(serviceEntityId);
        authnRequest.setIssuer(issuer);

        authnRequest.setSignature(createSignature());

        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnRequest).marshall(authnRequest);
            Signer.signObject(authnRequest.getSignature());
        } catch (SignatureException | MarshallingException e) {
            throw new SAMLRuntimeException("Unknown problem while signing SAML object", e);
        }

        return authnRequest;
    }

    private Extensions createExtensions() {
        Extensions extensions = new ExtensionsBuilder().buildObject();
        Attribute versionsAttribute = new AttributeBuilder().buildObject();
        versionsAttribute.setName("Versions");
        versionsAttribute.getAttributeValues().add(createApplicationVersion());
        extensions.getUnknownXMLObjects().add(encrypt(versionsAttribute));
        return extensions;
    }

    private EncryptedAttribute encrypt(Attribute attribute) {
        try {
            return encrypterFactory.createEncrypter().encrypt(attribute);
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    private Version createApplicationVersion() {
        ApplicationVersion applicationVersion = new ApplicationVersionImpl();

        String applicationManifestVersion = "UNKNOWN_VERSION";
        try {
            applicationManifestVersion = manifestReader.getAttributeValueFor(VerifyServiceProviderApplication.class, "Version");
        } catch (IOException e) {
            LOG.warn("Failed to read version number from the manifest");
        }

        applicationVersion.setValue(applicationManifestVersion);
        Version version = new VersionImpl() {{
            setApplicationVersion(applicationVersion);
        }};
        return version;
    }

    private Signature createSignature() {
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, Collections.emptyList());
        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        DigestSHA256 digestAlgorithm = new DigestSHA256();
        SignatureFactory signatureFactory = new SignatureFactory(keyStoreCredentialRetriever, signatureAlgorithm, digestAlgorithm);
        return signatureFactory.createSignature();
    }
}
