package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import gov.cms.madie.cql_elm_translator.service.CqlLibraryService;
import gov.cms.mat.cql.elements.UsingProperties;
import kotlinx.io.Source;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MadieLibrarySourceProviderTest {

  @AfterEach
  void tearDown() {
    MadieLibrarySourceProvider.setCqlLibraryService(null);
    MadieLibrarySourceProvider.setAccessToken(null);
  }

  @Test
  void getLibrarySourceShouldRetrieveCqlUsingLibraryIdentifierAndAccessToken() {
    // given - mocks
    CqlLibraryService cqlLibraryService = mock(CqlLibraryService.class);
    VersionedIdentifier libraryIdentifier = new VersionedIdentifier();
    libraryIdentifier.setId("IncludedLibrary");
    libraryIdentifier.setVersion("1.2.3");
    MadieLibrarySourceProvider.setCqlLibraryService(cqlLibraryService);
    MadieLibrarySourceProvider.setAccessToken("ACCESS_TOKEN");
    when(cqlLibraryService.getLibraryCql("IncludedLibrary", "1.2.3", null, "ACCESS_TOKEN"))
        .thenReturn("library IncludedLibrary version '1.2.3'");

    // when - call method under test
    Source result = new MadieLibrarySourceProvider().getLibrarySource(libraryIdentifier);

    // then - assertions
    assertThat(result, is(notNullValue()));
    verify(cqlLibraryService).getLibraryCql("IncludedLibrary", "1.2.3", null, "ACCESS_TOKEN");
  }

  @Test
  void getLibrarySourceShouldReturnNullWhenLibraryCqlIsMissing() {
    // given - mocks
    CqlLibraryService cqlLibraryService = mock(CqlLibraryService.class);
    VersionedIdentifier libraryIdentifier = new VersionedIdentifier();
    libraryIdentifier.setId("MissingLibrary");
    libraryIdentifier.setVersion("4.5.6");
    MadieLibrarySourceProvider.setCqlLibraryService(cqlLibraryService);
    MadieLibrarySourceProvider.setAccessToken("ACCESS_TOKEN");
    when(cqlLibraryService.getLibraryCql("MissingLibrary", "4.5.6", "ACCESS_TOKEN"))
        .thenReturn(null);

    // when - call method under test
    Source result = new MadieLibrarySourceProvider().getLibrarySource(libraryIdentifier);

    // then - assertions
    assertThat(result, is(nullValue()));
    verify(cqlLibraryService).getLibraryCql("MissingLibrary", "4.5.6", null, "ACCESS_TOKEN");
  }

  @Test
  void getLibrarySourcePassesNamespaceCanonical() {
    CqlLibraryService cqlLibraryService = mock(CqlLibraryService.class);
    MadieLibrarySourceProvider.setCqlLibraryService(cqlLibraryService);
    MadieLibrarySourceProvider.setUsing(
        UsingProperties.builder().libraryType("QICore").version("4.1.1").build());
    MadieLibrarySourceProvider.setAccessToken("Bearer token");

    VersionedIdentifier libraryIdentifier = new VersionedIdentifier();
    libraryIdentifier.setId("FHIRHelpers");
    libraryIdentifier.setVersion("4.0.001");
    libraryIdentifier.setSystem("http://hl7.org/fhir/us/qicore");

    when(cqlLibraryService.getLibraryCql(
            eq("FHIRHelpers"),
            eq("4.0.001"),
            eq("http://hl7.org/fhir/us/qicore"),
            eq("Bearer token")))
        .thenReturn("library FHIRHelpers version '4.0.001'\nusing FHIR version '4.0.1'");

    Assert.assertNotNull(new MadieLibrarySourceProvider().getLibrarySource(libraryIdentifier));
    verify(cqlLibraryService)
        .getLibraryCql("FHIRHelpers", "4.0.001", "http://hl7.org/fhir/us/qicore", "Bearer token");
  }
}
