package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import org.cqframework.cql.cql2elm.ModelManager;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

class TranslationResourceTest {

  private static ModelManager mockModelManager;

  @BeforeAll
  static void setUp() {
    mockModelManager = new ModelManager();
  }

  @Test
  void givenModelManagerAndIsFhirTrue_whenGetInstance_thenReturnsFhirTranslationResource() {
    // Given
    boolean isFhir = true;

    // When
    TranslationResource resource = TranslationResource.getInstance(mockModelManager, isFhir);

    // Then
    MatcherAssert.assertThat(resource, Matchers.notNullValue());
    MatcherAssert.assertThat(resource.getLibraryManager(), Matchers.notNullValue());
    MatcherAssert.assertThat(
        resource.getLibraryManager().getModelManager(), Matchers.sameInstance(mockModelManager));
  }

  @Test
  void givenModelManagerAndIsFhirFalse_whenGetInstance_thenReturnsQdmTranslationResource() {
    // Given
    boolean isFhir = false;

    // When
    TranslationResource resource = TranslationResource.getInstance(mockModelManager, isFhir);

    // Then
    MatcherAssert.assertThat(resource, Matchers.notNullValue());
    MatcherAssert.assertThat(resource.getLibraryManager(), Matchers.notNullValue());
    MatcherAssert.assertThat(
        resource.getLibraryManager().getModelManager(), Matchers.sameInstance(mockModelManager));
  }

  @Test
  void givenSingletonInstance_whenGetInstanceAgain_thenReturnsSameInstance() {
    // Given
    TranslationResource firstInstance = TranslationResource.getInstance(mockModelManager, true);

    // When
    TranslationResource secondInstance = TranslationResource.getInstance(mockModelManager, false);

    // Then
    MatcherAssert.assertThat(secondInstance, Matchers.sameInstance(firstInstance));
  }
}
