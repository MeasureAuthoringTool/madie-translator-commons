package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import gov.cms.madie.cql_elm_translator.utils.cql.data.RequestData;
import kotlinx.io.Source;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.cql.model.NamespaceInfo;
import org.hl7.cql.model.ModelIdentifier;
import org.hl7.elm_modelinfo.r1.ModelInfo;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
    globalCache = Map.of(new ModelIdentifier("TestModel", null, "1.0.0"), mockModel);
    mockModelManager = new ModelManager(globalCache);
  }

  @Test
  void testGetInstanceReturnsFhirTranslationResource() {
    // Given
    boolean isFhir = true;

    // When
    TranslationResource resource = new TranslationResource(mockModelManager, isFhir);

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
    TranslationResource resource = new TranslationResource(mockModelManager, isFhir);

    // Then
    assertThat(resource, notNullValue());
    assertThat(resource.getLibraryManager(), notNullValue());
    ModelManager modelManager = resource.getLibraryManager().getModelManager();
    assertThat(modelManager, notNullValue());
    assertThat(modelManager.resolveModel("TestModel", "1.0.0"), notNullValue());
  }

  @Test
  void testBuildTranslatorPassesNamespaceInfoFromRequestData() {
    // given
    NamespaceInfo namespaceInfo =
        new NamespaceInfo("hl7.fhir.us.qicore", "http://hl7.org/fhir/us/qicore");
    RequestData requestData =
        RequestData.builder()
            .cqlData("library Test version '1.0.0'")
            .errorSeverity(CqlCompilerException.ErrorSeverity.Info)
            .annotations(true)
            .locators(true)
            .disableListDemotion(true)
            .disableListPromotion(true)
            .disableMethodInvocation(false)
            .validateUnits(true)
            .resultTypes(true)
            .signatures(LibraryBuilder.SignatureLevel.Overloads)
            .nsInfo(namespaceInfo)
            .build();
    TranslationResource resource = new TranslationResource(mockModelManager, true);
    CqlTranslator expectedTranslator = mock(CqlTranslator.class);

    try (MockedStatic<CqlTranslator> translator = mockStatic(CqlTranslator.class)) {
      translator
          .when(
              () ->
                  CqlTranslator.fromSource(
                      same(namespaceInfo),
                      isNull(),
                      any(Source.class),
                      same(resource.getLibraryManager())))
          .thenReturn(expectedTranslator);

      // when
      CqlTranslator result = resource.buildTranslator(requestData);

      // then
      assertSame(expectedTranslator, result);
      translator.verify(
          () ->
              CqlTranslator.fromSource(
                  same(namespaceInfo),
                  isNull(),
                  any(Source.class),
                  same(resource.getLibraryManager())));
    }
  }
}
