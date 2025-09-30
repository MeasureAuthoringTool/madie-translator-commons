package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.cql.model.ModelIdentifier;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

class TranslationResourceTest {

  private static ModelManager mockModelManager;
  private static Map<ModelIdentifier, Model> globalCache;

  @BeforeAll
  static void setUp() {
    Model mockModel = Mockito.mock(Model.class);
    ModelInfo mockModelInfo = Mockito.mock(ModelInfo.class);
    when(mockModelInfo.getUrl()).thenReturn("http://test.org/test/model");
    when(mockModel.getModelInfo()).thenReturn(mockModelInfo);
    when(mockModelInfo.getVersion()).thenReturn("1.0.0");
    globalCache = Map.of(new ModelIdentifier().withId("TestModel").withVersion("1.0.0"), mockModel);
    mockModelManager = new ModelManager(globalCache);
  }

  @Test
  void testGetInstanceReturnsFhirTranslationResource() {
    // Given
    boolean isFhir = true;

    // When
    TranslationResource resource = TranslationResource.getInstance(mockModelManager, isFhir);

    // Then
    assertThat(resource, notNullValue());
    assertThat(resource.getLibraryManager(), notNullValue());
    ModelManager modelManager = resource.getLibraryManager().getModelManager();
    assertThat(modelManager, notNullValue());
    assertThat(modelManager.resolveModel("TestModel", "1.0.0"), notNullValue());
  }

  @Test
  void testGetInstanceReturnsQdmTranslationResource() {
    // Given
    boolean isFhir = false;

    // When
    TranslationResource resource = TranslationResource.getInstance(mockModelManager, isFhir);

    // Then
    assertThat(resource, notNullValue());
    assertThat(resource.getLibraryManager(), notNullValue());
    ModelManager modelManager = resource.getLibraryManager().getModelManager();
    assertThat(modelManager, notNullValue());
    assertThat(modelManager.resolveModel("TestModel", "1.0.0"), notNullValue());
  }
}
