package uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.Lists;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.metadata.EidasMetadataResolverRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class VerifyAndEidasCredentialResolver implements CredentialResolver {

    private final ExplicitKeySignatureTrustEngine hubTrustEngine;
    private final EidasMetadataResolverRepository eidasMetadataResolverRepository;

    public VerifyAndEidasCredentialResolver(ExplicitKeySignatureTrustEngine hubTrustEngine, EidasMetadataResolverRepository eidasMetadataResolverRepository) {
        this.hubTrustEngine = hubTrustEngine;
        this.eidasMetadataResolverRepository = eidasMetadataResolverRepository;
    }

    @Nonnull
    @Override
    public Iterable<Credential> resolve(@Nullable CriteriaSet criteria) throws ResolverException {
        List<Credential> credentialsFound = new LinkedList<>();
        if (criteria == null) {
            return credentialsFound;
        }

        for (String eidasResolverEntityId : eidasMetadataResolverRepository.getResolverEntityIds()) {
            eidasMetadataResolverRepository.getSignatureTrustEngine(eidasResolverEntityId).ifPresent(engine -> {
                try {
                    CredentialResolver eidasCredentialResolver = engine.getCredentialResolver();
                    credentialsFound.addAll(Lists.newLinkedList(eidasCredentialResolver.resolve(criteria)));
                } catch (ResolverException e) {
                    // NOP - could not resolve the criteria
                }
            });
        }

        CredentialResolver hubCredentialResolver = hubTrustEngine.getCredentialResolver();
        credentialsFound.addAll(Lists.newLinkedList(hubCredentialResolver.resolve(criteria)));
        return credentialsFound;
    }

    @Nullable
    @Override
    public Credential resolveSingle(@Nullable CriteriaSet criteria) throws ResolverException {
        Iterable<Credential> credentials = resolve(criteria);
        if (credentials.iterator().hasNext()) {
            Credential credential = credentials.iterator().next();
            if (credentials.iterator().hasNext()) {
                throw new ResolverException("More than 1 credential found for criteria provided.");
            }
            return credential;
        } else {
            return null;
        }
    }
}
