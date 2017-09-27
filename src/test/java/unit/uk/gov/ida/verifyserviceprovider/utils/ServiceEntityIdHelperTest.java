package unit.uk.gov.ida.verifyserviceprovider.utils;

import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdException;
import uk.gov.ida.verifyserviceprovider.utils.ServiceEntityIdHelper;

import java.util.Arrays;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

public class ServiceEntityIdHelperTest {
    private final String entityId = "http://default.entity.id";
    private final String otherEntityId = "http://other.provided.entity.id";

    @Test
    public void ShouldReturnDefaultEntityIdWhenNoneProvidedForSingleTenant_GenerateRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, null);

        assertThat(serviceEntityIdHelper.getEntityId(requestGenerationBody)).isEqualTo(entityId);
    }

    @Test
    public void ShouldReturnDefaultEntityIdWhenNoneProvidedForSingleTenant_TranslateResponse() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, null);

        assertThat(serviceEntityIdHelper.getEntityId(translateSamlResponseBody)).isEqualTo(entityId);
    }

    @Test
    public void ShouldReturnProvidedEntityIdWhenItIsInConfig_GenerateRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId, otherEntityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, otherEntityId);

        assertThat(serviceEntityIdHelper.getEntityId(requestGenerationBody)).isEqualTo(otherEntityId);
    }

    @Test
    public void ShouldReturnProvidedEntityIdWhenItIsInConfig_AuthnRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId, otherEntityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, otherEntityId);

        assertThat(serviceEntityIdHelper.getEntityId(translateSamlResponseBody)).isEqualTo(otherEntityId);
    }

    @Test
    public void ShouldReturnProvidedEntityIdWhenItIsTheOnlyOneInConfig_GenerateRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, entityId);

        assertThat(serviceEntityIdHelper.getEntityId(requestGenerationBody)).isEqualTo(entityId);
    }

    @Test
    public void ShouldReturnProvidedEntityIdWhenItIsTheOnlyOneInConfig_AuthnRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, entityId);

        assertThat(serviceEntityIdHelper.getEntityId(translateSamlResponseBody)).isEqualTo(entityId);
    }

    @Test
    public void ShouldThrowInvalidEntityIdExceptionWhenNoEntityIdIsProvidedForMultipleTenancy_GenerateRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId, otherEntityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, null);

        assertThatThrownBy(() -> serviceEntityIdHelper.getEntityId(requestGenerationBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("No entityId was provided, and there are several in config");
    }

    @Test
    public void ShouldThrowInvalidEntityIdExceptionWhenNoEntityIdIsProvidedForMultipleTenancy_AuthnRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId, otherEntityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, null);

        assertThatThrownBy(() -> serviceEntityIdHelper.getEntityId(translateSamlResponseBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("No entityId was provided, and there are several in config");
    }

    @Test
    public void ShouldThrowInvalidEntityIdExceptionWhenEntityIdProvidedIsNotInConfig_GenerateRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId, otherEntityId));
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(null, "http://some.other.entity.id");

        assertThatThrownBy(() -> serviceEntityIdHelper.getEntityId(requestGenerationBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("Provided entityId: http://some.other.entity.id is not listed in config");
    }

    @Test
    public void ShouldThrowInvalidEntityIdExceptionWhenEntityIdProvidedIsNotInConfig_AuthnRequest() {
        ServiceEntityIdHelper serviceEntityIdHelper = new ServiceEntityIdHelper(Arrays.asList(entityId, otherEntityId));
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(null, null, null, "http://some.other.entity.id");

        assertThatThrownBy(() -> serviceEntityIdHelper.getEntityId(translateSamlResponseBody))
            .isExactlyInstanceOf(InvalidEntityIdException.class)
            .hasMessage("Provided entityId: http://some.other.entity.id is not listed in config");
    }
}
