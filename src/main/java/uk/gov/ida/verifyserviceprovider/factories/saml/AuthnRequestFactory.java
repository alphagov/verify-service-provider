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
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.factories.EncrypterFactory;
import uk.gov.ida.verifyserviceprovider.saml.extensions.ApplicationVersion;
import uk.gov.ida.verifyserviceprovider.saml.extensions.ApplicationVersionImpl;
import uk.gov.ida.verifyserviceprovider.saml.extensions.Version;
import uk.gov.ida.verifyserviceprovider.saml.extensions.VersionImpl;
import uk.gov.ida.verifyserviceprovider.utils.ManifestReader;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.UUID;

import static uk.gov.ida.verifyserviceprovider.utils.Crypto.publicKeyFromPrivateKey;

public class AuthnRequestFactory {

    private final URI destination;
    private final PrivateKey signingKey;
    private final ManifestReader manifestReader;
    private final EncrypterFactory encrypterFactory;

    public AuthnRequestFactory(
        URI destination,
        PrivateKey signingKey,
        ManifestReader manifestReader,
        EncrypterFactory encrypterFactory
    ) {
        this.destination = destination;
        this.signingKey = signingKey;
        this.manifestReader = manifestReader;
        this.encrypterFactory = encrypterFactory;
    }

    public AuthnRequest build(LevelOfAssurance levelOfAssurance, String serviceEntityId) {
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
        Attribute attribute = new AttributeBuilder().buildObject();
        attribute.getAttributeValues().add(createApplicationVersion());
        extensions.getUnknownXMLObjects().add(encrypt(attribute));
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
        applicationVersion.setValue(manifestReader.getVersion());
        Version version = new VersionImpl() {{
            setApplicationVersion(applicationVersion);
        }};
        return version;
    }

    private Signature createSignature() {
        KeyPair signingKeyPair = new KeyPair(publicKeyFromPrivateKey(signingKey), signingKey);
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, Collections.emptyList());
        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        DigestSHA256 digestAlgorithm = new DigestSHA256();
        SignatureFactory signatureFactory = new SignatureFactory(keyStoreCredentialRetriever, signatureAlgorithm, digestAlgorithm);
        return signatureFactory.createSignature();
    }
}
