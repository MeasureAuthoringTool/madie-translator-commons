package gov.cms.madie.cql_elm_translator.utils.cql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.hl7.elm.r1.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.cms.madie.cql_elm_translator.utils.ResourceUtils;
import gov.cms.madie.cql_elm_translator.utils.cql.parsing.model.DefinitionContent;
import org.hl7.elm.r1.Library.Statements;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.ExpressionDef;

@ExtendWith(MockitoExtension.class)
class CQLToolsTest {

  @Mock private CqlTranslator cqlTranslator;
  @Mock private CompiledLibrary compiledLibrary;
  @Mock private Library library;
  @Mock private Element element;
  @Mock private Statements statements;

  VersionedIdentifier versionedIdentifier = new VersionedIdentifier();

  List<ExpressionDef> expressionDefinitions = new ArrayList<>();

  @Test
  void testGenerate() {
    versionedIdentifier.setId("local");
    String cqlData = ResourceUtils.getData("/tooling_test.cql");
    doReturn(statements).when(library).getStatements();
    doReturn(expressionDefinitions).when(statements).getDef();
    doReturn(compiledLibrary).when(cqlTranslator).getTranslatedLibrary();
    doReturn(library).when(compiledLibrary).getLibrary();
    doReturn(versionedIdentifier).when(compiledLibrary).getIdentifier();
    doReturn(element).when(compiledLibrary).resolve(any(String.class));
    Set<String> parentExpressions = new HashSet<>();
    CQLTools cqlTools = new CQLTools(cqlData, null, parentExpressions, cqlTranslator, null);
    assertNotNull(cqlTools);
    try {
      cqlTools.generate();
      Set<DefinitionContent> contents = cqlTools.getDefinitionContents();
      assertNotNull(contents);
    } catch (IOException e) {
      fail();
    }
  }
}
