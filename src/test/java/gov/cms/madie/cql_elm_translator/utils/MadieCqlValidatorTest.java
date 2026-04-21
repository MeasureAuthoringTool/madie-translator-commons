package gov.cms.madie.cql_elm_translator.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.VersionedIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.cms.madie.cql_elm_translator.exceptions.DuplicateIncludeCqlCompilerException;

@ExtendWith(MockitoExtension.class)
class MadieCqlValidatorTest {

  @Mock CqlTranslator mockTranslator;
  @Mock Library.Includes mockIncludes;
  @Mock List<CqlCompilerException> excepList;
  @Mock CompiledLibrary compiledLibrary;

  @Captor ArgumentCaptor<DuplicateIncludeCqlCompilerException> captor;

  List<IncludeDef> includes = new ArrayList<IncludeDef>();

  @Test
  void testNoConflict() {
    IncludeDef includeDef1 = new IncludeDef();
    includeDef1.setPath("ThisIsALibrary");
    includeDef1.setVersion("2.0.0");
    includeDef1.setLocator("5:1-5:65");

    includes.add(includeDef1);
    doReturn(includes).when(mockIncludes).getDef();
    VersionedIdentifier versionIdentifier = new VersionedIdentifier();
    MadieCqlValidator validation = new MadieCqlValidator();
    validation.checkNoDuplicateIncludes(mockTranslator, mockIncludes);

    DuplicateIncludeCqlCompilerException exception =
        new DuplicateIncludeCqlCompilerException("ThisIsALibrary", versionIdentifier, "2.0.0", 5);

    verify(excepList, times(0)).add(any(CqlCompilerException.class));
  }

  @Test
  void testSameLibrariesSameVersion() {
    IncludeDef includeDef1 = new IncludeDef();
    includeDef1.setPath("ThisIsALibrary");
    includeDef1.setVersion("2.0.0");
    includeDef1.setLocator("5:1-5:65");

    IncludeDef includeDef2 = new IncludeDef();
    includeDef2.setPath("ThisIsALibrary");
    includeDef2.setVersion("2.0.0");
    includeDef2.setLocator("6:1-6:65");

    includes.add(includeDef1);
    includes.add(includeDef2);
    doReturn(excepList).when(mockTranslator).getExceptions();
    doReturn(includes).when(mockIncludes).getDef();
    VersionedIdentifier versionIdentifier = new VersionedIdentifier();
    doReturn(versionIdentifier).when(compiledLibrary).getIdentifier();
    doReturn(compiledLibrary).when(mockTranslator).getTranslatedLibrary();
    MadieCqlValidator validation = new MadieCqlValidator();
    validation.checkNoDuplicateIncludes(mockTranslator, mockIncludes);

    DuplicateIncludeCqlCompilerException exception =
        new DuplicateIncludeCqlCompilerException("ThisIsALibrary", versionIdentifier, "2.0.0", 5);

    verify(excepList)
        .add(
            argThat(
                x -> {
                  assertEquals(
                      "Library ThisIsALibrary Version 2.0.0 is already in use in this library.",
                      ((DuplicateIncludeCqlCompilerException) x).getMessage());

                  return true;
                }));
  }

  @Test
  void testSameLibrariesDifferentVersion() {
    IncludeDef includeDef1 = new IncludeDef();
    includeDef1.setPath("ThisIsALibrary");
    includeDef1.setVersion("1.0.0");
    includeDef1.setLocator("5:1-5:65");

    IncludeDef includeDef2 = new IncludeDef();
    includeDef2.setPath("ThisIsALibrary");
    includeDef2.setVersion("2.0.0");
    includeDef2.setLocator("6:1-6:65");

    includes.add(includeDef1);
    includes.add(includeDef2);
    doReturn(excepList).when(mockTranslator).getExceptions();
    doReturn(includes).when(mockIncludes).getDef();
    VersionedIdentifier versionIdentifier = new VersionedIdentifier();
    doReturn(versionIdentifier).when(compiledLibrary).getIdentifier();
    doReturn(compiledLibrary).when(mockTranslator).getTranslatedLibrary();
    MadieCqlValidator validation = new MadieCqlValidator();
    validation.checkNoDuplicateIncludes(mockTranslator, mockIncludes);

    DuplicateIncludeCqlCompilerException exception =
        new DuplicateIncludeCqlCompilerException("ThisIsALibrary", versionIdentifier, "2.0.0", 5);

    verify(excepList)
        .add(
            argThat(
                x -> {
                  assertEquals(
                      "Library ThisIsALibrary is already in use in this library.",
                      ((DuplicateIncludeCqlCompilerException) x).getMessage());

                  return true;
                }));
  }
}
