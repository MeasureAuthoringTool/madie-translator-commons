package gov.cms.madie.cql_elm_translator.utils.cql.parsing.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CQLDefinitionTest {

  @Test
  void testUniqueness() {
    CQLDefinition def1 = CQLDefinition.builder().definitionName("SDE Race").build();
    CQLDefinition def2 = CQLDefinition.builder().definitionName("SDE Race").build();

    Set<CQLDefinition> defSet = new HashSet<>();

    defSet.add(def1);
    defSet.add(def2);

    assertEquals(1, defSet.size());
  }

  @Test
  void testUniquenessLibraryName() {
    CQLDefinition def1 = CQLDefinition.builder().definitionName("SDE Race").build();
    CQLDefinition def2 =
        CQLDefinition.builder().definitionName("SDE Race").libraryDisplayName("SDE").build();

    Set<CQLDefinition> defSet = new HashSet<>();

    defSet.add(def1);
    defSet.add(def2);

    assertEquals(2, defSet.size());
  }
}
