package unit.uk.gov.ida.verifyserviceprovider.saml;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.PersistentId;
import uk.gov.ida.verifyserviceprovider.exceptions.AuthnContextMissingException;
import uk.gov.ida.verifyserviceprovider.factories.saml.UserIdHashFactory;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.builders.PersistentIdBuilder.aPersistentId;


@RunWith(MockitoJUnitRunner.class)
public class UserIdHashFactoryTest {

    private static final String HASHING_ENTITY_ID = "entity";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private UserIdHashFactory userIdHashFactory = new UserIdHashFactory(HASHING_ENTITY_ID);

    @Test
    public void shouldPerformHashing() {
        final PersistentId persistentId = aPersistentId().build();
        final String issuerId = "partner";

        final String hashedId = userIdHashFactory.hashId(issuerId, persistentId.getNameId(), Optional.of(AuthnContext.LEVEL_2));

        assertThat(hashedId).isEqualTo("a5fbea969c3837a712cbe9e188804796828f369106478e623a436fa07e8fd298");
    }

    @Test
    public void shouldGenerateADifferentHashForEveryLevelOfAssurance(){
        final PersistentId persistentId = aPersistentId().build();
        final String partnerEntityId = "partner";

        final long numberOfUniqueGeneratedHashedPids = Arrays.stream(AuthnContext.values())
                .map(authnContext -> userIdHashFactory.hashId(partnerEntityId, persistentId.getNameId(), Optional.of(authnContext)))
                .distinct()
                .count();

        assertThat(numberOfUniqueGeneratedHashedPids).isEqualTo(5);
    }

    @Test
    public void shouldThrowErrorWhenAuthnContextAbsent() {
        exception.expect(AuthnContextMissingException.class);
        exception.expectMessage(String.format("Authn context absent for persistent id %s", "pid"));

        userIdHashFactory.hashId("", "pid", Optional.empty());
    }
}
