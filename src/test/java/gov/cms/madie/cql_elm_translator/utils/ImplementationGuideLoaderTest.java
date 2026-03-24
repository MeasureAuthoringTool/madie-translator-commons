package gov.cms.madie.cql_elm_translator.utils;

import org.hl7.fhir.r5.model.ImplementationGuide;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

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
          implementationGuide.getId(), is(equalTo("ImplementationGuide/cms.fhir.us.madieig")));
      assertThat(
          implementationGuide.getUrl(),
          is(
              equalTo(
                  "http://madie.cms.gov/fhir/us/madieig/ImplementationGuide/cms.fhir.us.madieig")));
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
}
