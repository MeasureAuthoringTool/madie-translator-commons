package gov.cms.madie.cql_elm_translator.utils.cql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.cql2elm.model.ResolvedIdentifierContext;
import org.hl7.cql.model.DataType;
import org.hl7.elm.r1.IncludeDef;
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
@Slf4j
class CQLToolsTest {

  @Mock private CqlTranslator cqlTranslator;
  @Mock private CompiledLibrary compiledLibrary;
  @Mock private Library library;
  @Mock private ResolvedIdentifierContext element;
  @Mock private Statements statements;
  @Mock private Library.Includes includes;
  @Mock private Map<String, CompiledLibrary> compiledLibraryMap;

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

  @Test
  void testItHandlesNullExpressionResultType() {
    versionedIdentifier.setId("local");
    String cqlData = ResourceUtils.getData("/tooling_test_qicore6.cql");
    doReturn(statements).when(library).getStatements();
    doReturn(compiledLibrary).when(cqlTranslator).getTranslatedLibrary();
    doReturn(library).when(compiledLibrary).getLibrary();
    doReturn(versionedIdentifier).when(compiledLibrary).getIdentifier();
    doReturn(element).when(compiledLibrary).resolve(any(String.class));
    doReturn(includes).when(library).getIncludes();

    ExpressionDef expression1 = mock(ExpressionDef.class);
    DataType booleanDataType = mock(DataType.class);
    doReturn(booleanDataType).when(expression1).getResultType();
    doReturn("expression1").when(expression1).getName();
    doReturn("System.Boolean").when(booleanDataType).toString();

    ExpressionDef expression2 = mock(ExpressionDef.class);
    doReturn("expression2").when(expression2).getName();
    doReturn(null).when(expression2).getResultType();

    List<ExpressionDef> expressionDefs = List.of(expression1, expression2);

    doReturn(expressionDefs).when(statements).getDef();

    Set<String> parentExpressions = new HashSet<>();
    IncludeDef includeDef1 = mock(IncludeDef.class);
    List<IncludeDef> includeDefs = List.of(includeDef1);
    doReturn(includeDefs).when(includes).getDef();
    doReturn("SupplementalDataElements").when(includeDef1).getPath();
    CompiledLibrary sdeCompiledLib = mock(CompiledLibrary.class);
    Library sdeLib = mock(Library.class);
    doReturn(sdeLib).when(sdeCompiledLib).getLibrary();
    VersionedIdentifier sdeLibVersionedIdentifier = new VersionedIdentifier();
    sdeLibVersionedIdentifier.setId("SupplementalDataElements");
    doReturn(sdeLibVersionedIdentifier).when(sdeCompiledLib).getIdentifier();

    Library.Statements statements1 = mock(Library.Statements.class);
    doReturn(statements1).when(sdeLib).getStatements();
    Library.Parameters parameters1 = mock(Library.Parameters.class);
    doReturn(parameters1).when(sdeLib).getParameters();

    doReturn(sdeCompiledLib).when(compiledLibraryMap).get(anyString());

    CQLTools cqlTools =
        new CQLTools(cqlData, null, parentExpressions, cqlTranslator, compiledLibraryMap);
    assertNotNull(cqlTools);
    try {
      cqlTools.generate();
      Set<DefinitionContent> contents = cqlTools.getDefinitionContents();
      assertNotNull(contents);
      Map<String, String> expressionToReturnTypeMap = cqlTools.getExpressionToReturnTypeMap();
      assertThat(expressionToReturnTypeMap, is(notNullValue()));
      assertThat(expressionToReturnTypeMap.containsKey("expression1"), is(true));
      assertThat(expressionToReturnTypeMap.containsKey("expression2"), is(true));
      assertThat(expressionToReturnTypeMap.get("expression1"), is(equalTo("System.Boolean")));
      assertThat(expressionToReturnTypeMap.get("expression2"), is(nullValue()));
    } catch (IOException e) {
      fail();
    }
  }
}
