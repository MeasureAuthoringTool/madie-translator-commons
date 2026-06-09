package gov.cms.madie.cql_elm_translator.utils;

import org.hl7.fhir.r5.model.ImplementationGuide;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ImplementationGuideLoaderTest {

  @Test
  void loadImplementationGuideShouldReturnGuideWithExpectedAttributes() {
    // given
    String resourcePath = "igs/qicore-7-madie-ig.json";
    try (InputStream inputStream =
        ImplementationGuideLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {

      // when
      ImplementationGuide implementationGuide =
          ImplementationGuideLoader.parseFromInputStream(inputStream);

      // then
      assertThat(implementationGuide, is(notNullValue()));
      assertThat(
          implementationGuide.getId(),
          is(equalTo("ImplementationGuide/cms.fhir.us.madie.qicore7ig")));
      assertThat(
          implementationGuide.getUrl(),
          is(
              equalTo(
                  "http://madie.cms.gov/fhir/us/madieig/ImplementationGuide/cms.fhir.us.madie.qicore7ig")));
      assertThat(implementationGuide.getContactFirstRep().getName(), is(equalTo("CMS")));
    } catch (java.io.IOException e) {
      Assertions.fail("Failed to load IG from resource path: " + resourcePath, e);
    }
  }

  @Test
  void loadImplementationGuideShouldIncludeDependencies() {
    // given
    String resourcePath = "igs/qicore-7-madie-ig.json";
    try (InputStream inputStream =
        ImplementationGuideLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {

      // when
      ImplementationGuide implementationGuide =
          ImplementationGuideLoader.parseFromInputStream(inputStream);

      // then
      assertThat(implementationGuide.getDependsOn(), hasSize(greaterThanOrEqualTo(2)));
      assertThat(
          implementationGuide.getDependsOn().get(0).getPackageId(),
          is(equalTo("hl7.fhir.us.qicore")));
    } catch (java.io.IOException e) {
      Assertions.fail("Failed to load IG from resource path: " + resourcePath, e);
    }
  }

  @Test
  void loadShouldReturnAllMatchingImplementationGuides() {
    // when
    List<ImplementationGuide> igs = ImplementationGuideLoader.load("classpath*:igs/*.json");

    // then
    assertThat(igs, is(notNullValue()));
    assertThat(igs, hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  void loadShouldReturnEmptyListForNonMatchingPattern() {
    // when
    List<ImplementationGuide> igs =
        ImplementationGuideLoader.load("classpath*:igs/nonexistent-*.json");

    // then
    assertThat(igs, is(notNullValue()));
    assertThat(igs, is(empty()));
  }

  @Test
  void loadShouldReturnSpecificImplementationGuide() {
    // when
    List<ImplementationGuide> igs =
        ImplementationGuideLoader.load("classpath*:igs/madie-test-ig.json");

    // then
    assertThat(igs, hasSize(1));
    assertThat(igs.get(0).getId(), is(equalTo("ImplementationGuide/cms.fhir.us.test.madieig")));
  }

  @Test
  void buildPackageManagerShouldSucceedWithExplicitCachePath(@TempDir Path tempDir) {
    // given — use an IG with no package dependencies to avoid network calls
    List<ImplementationGuide> igs =
        ImplementationGuideLoader.load("classpath*:igs/madie-nodeps-test-ig.json");
    assertThat(igs, hasSize(1));
    ImplementationGuide ig = igs.get(0);

    // when / then — should not throw with a dedicated temp cache path
    try {
      var packageManager = ImplementationGuideLoader.buildPackageManager(tempDir.toString(), ig);
      assertThat(packageManager, is(notNullValue()));
    } catch (Exception e) {
      Assertions.fail("buildPackageManager threw an unexpected exception: " + e.getMessage(), e);
    }
  }
}
