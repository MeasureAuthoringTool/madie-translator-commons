package gov.cms.madie.cql_elm_translator.utils;

import gov.cms.mat.cql.elements.UsingProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

class FhirUtilTest {
  private final FhirUtil fhirUtil = new FhirUtil();

  @Test
  void isFhirModelShouldReturnTrueForFhirModels() {
    // given
    // no mocks needed

    // when
    boolean fhir = fhirUtil.isFhirModel("FHIR");
    boolean uscore = fhirUtil.isFhirModel("uscore");
    boolean qicore = fhirUtil.isFhirModel("QICORE");

    // then
    assertThat(fhir, is(true));
    assertThat(uscore, is(true));
    assertThat(qicore, is(true));
  }

  @Test
  void isFhirModelShouldReturnFalseForUnknownModels() {
    // given
    // no mocks needed

    // when
    boolean result = fhirUtil.isFhirModel("notamodel");

    // then
    assertThat(result, is(false));
  }

  @Test
  void isFhirModelShouldReturnFalseForNullOrEmpty() {
    // given
    // no mocks needed

    // when
    boolean nullResult = fhirUtil.isFhirModel(null);
    boolean emptyResult = fhirUtil.isFhirModel("");

    // then
    assertThat(nullResult, is(false));
    assertThat(emptyResult, is(false));
  }

  @Test
  void getMostSpecificFhirModelShouldReturnMostSpecific() {
    // given
    UsingProperties fhir = Mockito.mock(UsingProperties.class);
    when(fhir.getLibraryType()).thenReturn("FHIR");
    UsingProperties uscore = Mockito.mock(UsingProperties.class);
    when(uscore.getLibraryType()).thenReturn("USCore");
    UsingProperties qicore = Mockito.mock(UsingProperties.class);
    when(qicore.getLibraryType()).thenReturn("QICore");
    List<UsingProperties> list = Arrays.asList(fhir, uscore, qicore);

    // when
    UsingProperties result = fhirUtil.getMostSpecificFhirModel(list);

    // then
    assertThat(result, is(equalTo(qicore)));
  }

  @Test
  void getMostSpecificFhirModelShouldReturnNullIfNoneMatch() {
    // given
    UsingProperties notFhir = Mockito.mock(UsingProperties.class);
    when(notFhir.getLibraryType()).thenReturn("NotAModel");
    List<UsingProperties> list = Collections.singletonList(notFhir);

    // when
    UsingProperties result = fhirUtil.getMostSpecificFhirModel(list);

    // then
    assertThat(result, is(nullValue()));
  }

  @Test
  void getMostSpecificFhirModelShouldIgnoreUnknownModelAndReturnQiCore() {
    // given
    UsingProperties notFhir = Mockito.mock(UsingProperties.class);
    when(notFhir.getLibraryType()).thenReturn("NotAModel");
    UsingProperties qicore = Mockito.mock(UsingProperties.class);
    when(qicore.getLibraryType()).thenReturn("QICore");
    List<UsingProperties> list = Arrays.asList(notFhir, qicore);

    // when
    UsingProperties result = fhirUtil.getMostSpecificFhirModel(list);

    // then
    assertThat(result, is(equalTo(qicore)));
  }

  @Test
  void getMostSpecificFhirModelShouldReturnNullForNullOrEmptyList() {
    // given
    // no mocks needed

    // when
    UsingProperties nullResult = fhirUtil.getMostSpecificFhirModel(null);
    UsingProperties emptyResult = fhirUtil.getMostSpecificFhirModel(Collections.emptyList());

    // then
    assertThat(nullResult, is(nullValue()));
    assertThat(emptyResult, is(nullValue()));
  }

  @Test
  void getMostSpecificFhirModelShouldSkipNullEntriesAndChooseSpecific() {
    // given
    UsingProperties qicore = Mockito.mock(UsingProperties.class);
    when(qicore.getLibraryType()).thenReturn("QICore");
    List<UsingProperties> list = Arrays.asList(null, qicore);

    // when
    UsingProperties result = fhirUtil.getMostSpecificFhirModel(list);

    // then
    assertThat(result, is(equalTo(qicore)));
  }

  @Test
  void getMostSpecificFhirModelShouldSkipEntriesWithNullLibraryType() {
    // given
    UsingProperties nullType = Mockito.mock(UsingProperties.class);
    when(nullType.getLibraryType()).thenReturn(null);
    UsingProperties qicore = Mockito.mock(UsingProperties.class);
    when(qicore.getLibraryType()).thenReturn("QICore");
    List<UsingProperties> list = Arrays.asList(nullType, qicore);

    // when
    UsingProperties result = fhirUtil.getMostSpecificFhirModel(list);

    // then
    assertThat(result, is(equalTo(qicore)));
  }

  @Test
  void getMostSpecificFhirModelShouldKeepFirstMostSpecificWhenLessSpecificFollows() {
    // given
    UsingProperties qicore = Mockito.mock(UsingProperties.class);
    when(qicore.getLibraryType()).thenReturn("QICore");
    UsingProperties uscore = Mockito.mock(UsingProperties.class);
    when(uscore.getLibraryType()).thenReturn("USCore");
    List<UsingProperties> list = Arrays.asList(qicore, uscore);

    // when
    UsingProperties result = fhirUtil.getMostSpecificFhirModel(list);

    // then
    assertThat(result, is(equalTo(qicore)));
  }

  @Test
  void isMeasureCompatibleWithLibraryShouldReturnTrueForSameModel() {
    // given
    // no mocks needed

    // when
    boolean result = fhirUtil.isMeasureCompatibleWithLibrary("QICORE", "QICORE");

    // then
    assertThat(result, is(true));
  }

  @Test
  void isMeasureCompatibleWithLibraryShouldReturnTrueWhenMeasureIsMoreSpecificThanLibrary() {
    // given
    // no mocks needed

    // when
    boolean qiCoreWithFhir = fhirUtil.isMeasureCompatibleWithLibrary("QICORE", "FHIR");
    boolean qiCoreWithUsCore = fhirUtil.isMeasureCompatibleWithLibrary("QICORE", "USCORE");
    boolean usCoreWithFhir = fhirUtil.isMeasureCompatibleWithLibrary("USCORE", "FHIR");
    boolean usQualityCoreWithFhir =
        fhirUtil.isMeasureCompatibleWithLibrary("USQUALITYCORE", "FHIR");
    boolean usQualityCoreWithUsCore =
        fhirUtil.isMeasureCompatibleWithLibrary("USQUALITYCORE", "USCORE");

    // then
    assertThat(qiCoreWithFhir, is(true));
    assertThat(qiCoreWithUsCore, is(true));
    assertThat(usCoreWithFhir, is(true));
    assertThat(usQualityCoreWithFhir, is(true));
    assertThat(usQualityCoreWithUsCore, is(true));
  }

  @Test
  void isMeasureCompatibleWithLibraryShouldReturnFalseWhenLibraryIsMoreSpecificThanMeasure() {
    // given
    // no mocks needed

    // when
    boolean fhirMeasureWithQiCoreLib = fhirUtil.isMeasureCompatibleWithLibrary("FHIR", "QICORE");
    boolean usCoreWithQiCore = fhirUtil.isMeasureCompatibleWithLibrary("USCORE", "QICORE");
    boolean qiCoreWithUsQualityCore =
        fhirUtil.isMeasureCompatibleWithLibrary("QICORE", "USQUALITYCORE");
    boolean usQualityCoreWithQiCore =
        fhirUtil.isMeasureCompatibleWithLibrary("USQUALITYCORE", "QICORE");

    // then
    assertThat(fhirMeasureWithQiCoreLib, is(false));
    assertThat(usCoreWithQiCore, is(false));
    assertThat(qiCoreWithUsQualityCore, is(false));
    assertThat(usQualityCoreWithQiCore, is(false));
  }

  @Test
  void isMeasureCompatibleWithLibraryShouldReturnFalseForNullInputs() {
    // given
    // no mocks needed

    // when
    boolean nullMeasure = fhirUtil.isMeasureCompatibleWithLibrary(null, "QICORE");
    boolean nullLibrary = fhirUtil.isMeasureCompatibleWithLibrary("QICORE", null);
    boolean bothNull = fhirUtil.isMeasureCompatibleWithLibrary(null, null);

    // then
    assertThat(nullMeasure, is(false));
    assertThat(nullLibrary, is(false));
    assertThat(bothNull, is(false));
  }

  @Test
  void isMeasureCompatibleWithLibraryShouldReturnFalseForUnknownModels() {
    // given
    // no mocks needed

    // when
    boolean unknownMeasure = fhirUtil.isMeasureCompatibleWithLibrary("NOTAMODEL", "FHIR");
    boolean unknownLibrary = fhirUtil.isMeasureCompatibleWithLibrary("QICORE", "NOTAMODEL");

    // then
    assertThat(unknownMeasure, is(false));
    assertThat(unknownLibrary, is(false));
  }

  @Test
  void fhirModelVersionsAreConsistentShouldReturnTrueWhenNoModelNameOverlap() {
    // Measure: QICore 4.1.1 — Library: USCore 6.0.0 → no overlap → true
    UsingProperties measureQiCore = Mockito.mock(UsingProperties.class);
    when(measureQiCore.getLibraryType()).thenReturn("QICore");
    when(measureQiCore.getVersion()).thenReturn("4.1.1");

    UsingProperties libraryUsCore = Mockito.mock(UsingProperties.class);
    when(libraryUsCore.getLibraryType()).thenReturn("USCore");
    when(libraryUsCore.getVersion()).thenReturn("6.0.0");

    boolean result =
        fhirUtil.fhirModelVersionsAreConsistent(List.of(measureQiCore), List.of(libraryUsCore));

    assertThat(result, is(true));
  }

  @Test
  void fhirModelVersionsAreConsistentShouldReturnTrueWhenOverlappingModelsSameVersion() {
    // Measure: QICore 4.1.1 — Library: QICore 4.1.1 → overlap, same version → true
    UsingProperties measureQiCore = Mockito.mock(UsingProperties.class);
    when(measureQiCore.getLibraryType()).thenReturn("QICore");
    when(measureQiCore.getVersion()).thenReturn("4.1.1");

    UsingProperties libraryQiCore = Mockito.mock(UsingProperties.class);
    when(libraryQiCore.getLibraryType()).thenReturn("QICore");
    when(libraryQiCore.getVersion()).thenReturn("4.1.1");

    boolean result =
        fhirUtil.fhirModelVersionsAreConsistent(List.of(measureQiCore), List.of(libraryQiCore));

    assertThat(result, is(true));
  }

  @Test
  void fhirModelVersionsAreConsistentShouldReturnFalseWhenOverlappingModelsHaveDifferentVersions() {
    // Measure: QICore 4.1.1 — Library: QICore 6.0.0 → overlap, different versions → false
    UsingProperties measureQiCore = Mockito.mock(UsingProperties.class);
    when(measureQiCore.getLibraryType()).thenReturn("QICore");
    when(measureQiCore.getVersion()).thenReturn("4.1.1");

    UsingProperties libraryQiCore = Mockito.mock(UsingProperties.class);
    when(libraryQiCore.getLibraryType()).thenReturn("QICore");
    when(libraryQiCore.getVersion()).thenReturn("6.0.0");

    boolean result =
        fhirUtil.fhirModelVersionsAreConsistent(List.of(measureQiCore), List.of(libraryQiCore));

    assertThat(result, is(false));
  }

  @Test
  void fhirModelVersionsAreConsistentShouldReturnFalseForUsCoreVersionMismatch() {
    // Measure: USCore 6.0.0 — Library: USCore 7.0.0 → false
    UsingProperties measureUsCore = Mockito.mock(UsingProperties.class);
    when(measureUsCore.getLibraryType()).thenReturn("USCore");
    when(measureUsCore.getVersion()).thenReturn("6.0.0");

    UsingProperties libraryUsCore = Mockito.mock(UsingProperties.class);
    when(libraryUsCore.getLibraryType()).thenReturn("USCore");
    when(libraryUsCore.getVersion()).thenReturn("7.0.0");

    boolean result =
        fhirUtil.fhirModelVersionsAreConsistent(List.of(measureUsCore), List.of(libraryUsCore));

    assertThat(result, is(false));
  }

  @Test
  void fhirModelVersionsAreConsistentShouldReturnTrueWhenFhirOverlapMatchesAndQiCoreDiffers() {
    // Measure: QICore 4.1.1 + FHIR 4.0.1 — Library: FHIR 4.0.1 → FHIR overlaps with same version →
    // true
    UsingProperties measureQiCore = Mockito.mock(UsingProperties.class);
    when(measureQiCore.getLibraryType()).thenReturn("QICore");
    when(measureQiCore.getVersion()).thenReturn("4.1.1");

    UsingProperties measureFhir = Mockito.mock(UsingProperties.class);
    when(measureFhir.getLibraryType()).thenReturn("FHIR");
    when(measureFhir.getVersion()).thenReturn("4.0.1");

    UsingProperties libraryFhir = Mockito.mock(UsingProperties.class);
    when(libraryFhir.getLibraryType()).thenReturn("FHIR");
    when(libraryFhir.getVersion()).thenReturn("4.0.1");

    boolean result =
        fhirUtil.fhirModelVersionsAreConsistent(
            List.of(measureQiCore, measureFhir), List.of(libraryFhir));

    assertThat(result, is(true));
  }

  @Test
  void fhirModelVersionsAreConsistentShouldReturnTrueForNullLists() {
    assertThat(fhirUtil.fhirModelVersionsAreConsistent(null, List.of()), is(true));
    assertThat(fhirUtil.fhirModelVersionsAreConsistent(List.of(), null), is(true));
    assertThat(fhirUtil.fhirModelVersionsAreConsistent(null, null), is(true));
  }

  @Test
  void fhirModelVersionsAreConsistentShouldIgnoreNonFhirModelsInMeasure() {
    // QDM in measure using list should be ignored; library QICore has no overlap → true
    UsingProperties measureQdm = Mockito.mock(UsingProperties.class);
    when(measureQdm.getLibraryType()).thenReturn("QDM");
    when(measureQdm.getVersion()).thenReturn("5.6");

    UsingProperties libraryQiCore = Mockito.mock(UsingProperties.class);
    when(libraryQiCore.getLibraryType()).thenReturn("QICore");
    when(libraryQiCore.getVersion()).thenReturn("4.1.1");

    boolean result =
        fhirUtil.fhirModelVersionsAreConsistent(List.of(measureQdm), List.of(libraryQiCore));

    assertThat(result, is(true));
  }

  @Test
  void getMostSpecificFhirModelShouldKeepFirstMostSpecificWhenLessSpecificFollowsZZZ() {
    // given
    UsingProperties qicore = Mockito.mock(UsingProperties.class);
    when(qicore.getLibraryType()).thenReturn("OtherModel");
    UsingProperties uscore = Mockito.mock(UsingProperties.class);
    when(uscore.getLibraryType()).thenReturn("USCore");
    UsingProperties bad = Mockito.mock(UsingProperties.class);
    when(bad.getLibraryType()).thenReturn("BadModel");
    List<UsingProperties> list = Arrays.asList(qicore, uscore, bad);
    Object map = ReflectionTestUtils.getField(FhirUtil.class, "MODEL_MAP");
    if (map instanceof Map) {
      Map<String, ModelNode> modelMap = (Map<String, ModelNode>) map;
      modelMap.put("BADMODEL", new ModelNode("BADMODEL", null));
    }

    // when
    UsingProperties result = fhirUtil.getMostSpecificFhirModel(list);

    // then
    assertThat(result, is(equalTo(uscore)));
  }
}
