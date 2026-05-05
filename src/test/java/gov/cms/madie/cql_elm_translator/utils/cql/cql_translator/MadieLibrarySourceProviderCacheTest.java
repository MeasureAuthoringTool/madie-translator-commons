package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import gov.cms.mat.cql.elements.UsingProperties;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MadieLibrarySourceProviderCacheTest {

  @Test
  void buildCacheKeyIncludesUsingVersionAndLibraryVersion() {
    MadieLibrarySourceProvider.setUsing(
        UsingProperties.builder()
            .libraryType("QICore")
            .version("4.1.1")
            .line("using QICore version '4.1.1'")
            .build());

    VersionedIdentifier identifier = new VersionedIdentifier();
    identifier.setId("FHIRHelpers");
    identifier.setVersion("1.0.000");

    assertEquals("FHIRHelpers-4.1.1-1.0.000", MadieLibrarySourceProvider.buildCacheKey(identifier));
  }
}
