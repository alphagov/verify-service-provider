package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdException;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EntityIdServiceTest {
    private final String entityId = "http://default.entity.id";
    private final String otherEntityId = "http://other.provided.entity.id";

    @Test
    public void shouldReturnDefaultEntityIdWhenNoneProvidedForSingleTenantForGenerateRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, null);

        assertThat(entityIdService.getEntityId(requestGenerationBody)).isEqualTo(entityId);
    }

    @Test
    public void shouldReturnDefaultEntityIdWhenNullBodyProvidedForSingleTenantForGenerateRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId));
        assertThat(entityIdService.getEntityId((RequestGenerationBody)null)).isEqualTo(entityId);
    }

    @Test
    public void shouldReturnDefaultEntityIdWhenNoneProvidedForSingleTenantForTranslateResponse() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, null);

        assertThat(entityIdService.getEntityId(translateSamlResponseBody)).isEqualTo(entityId);
    }

    @Test
    public void shouldReturnProvidedEntityIdWhenItIsInConfigForGenerateRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId, otherEntityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, otherEntityId);

        assertThat(entityIdService.getEntityId(requestGenerationBody)).isEqualTo(otherEntityId);
    }

    @Test
    public void shouldReturnProvidedEntityIdWhenItIsInConfigForAuthnRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId, otherEntityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, otherEntityId);

        assertThat(entityIdService.getEntityId(translateSamlResponseBody)).isEqualTo(otherEntityId);
    }

    @Test
    public void shouldReturnProvidedEntityIdWhenItIsTheOnlyOneInConfigForGenerateRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, entityId);

        assertThat(entityIdService.getEntityId(requestGenerationBody)).isEqualTo(entityId);
    }

    @Test
    public void shouldReturnProvidedEntityIdWhenItIsTheOnlyOneInConfigForAuthnRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, entityId);

        assertThat(entityIdService.getEntityId(translateSamlResponseBody)).isEqualTo(entityId);
    }

    @Test
    public void shouldThrowInvalidEntityIdExceptionWhenNoEntityIdIsProvidedForMultipleTenancyForGenerateRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId, otherEntityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, null);

        assertThatThrownBy(() -> entityIdService.getEntityId(requestGenerationBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("No entityId was provided, and there are several in config");
    }

    @Test
    public void shouldThrowInvalidEntityIdExceptionWhenNoEntityIdIsProvidedForMultipleTenancyForAuthnRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId, otherEntityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, null);

        assertThatThrownBy(() -> entityIdService.getEntityId(translateSamlResponseBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("No entityId was provided, and there are several in config");
    }

    @Test
    public void shouldThrowInvalidEntityIdExceptionWhenEntityIdProvidedIsNotInConfigForGenerateRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId, otherEntityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, "http://some.other.entity.id");

        assertThatThrownBy(() -> entityIdService.getEntityId(requestGenerationBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("Provided entityId: http://some.other.entity.id is not listed in config");
    }

    @Test
    public void shouldThrowInvalidEntityIdExceptionWhenEntityIdProvidedIsNotInConfigForAuthnRequest() {
        EntityIdService entityIdService = new EntityIdService(Arrays.asList(entityId, otherEntityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, "http://some.other.entity.id");

        assertThatThrownBy(() -> entityIdService.getEntityId(translateSamlResponseBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("Provided entityId: http://some.other.entity.id is not listed in config");
    }
}
