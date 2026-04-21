package gov.cms.madie.cql_elm_translator.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class CqlBuilderLookupComparatorTest {

  @Test
  void testAddOne() {

    CqlBuilderLookup.Lookup lookup =
        CqlBuilderLookup.Lookup.builder()
            .name("SDE Race")
            .libraryAlias("SDE")
            .libraryName("Supplemental Data")
            .startLine(11)
            .build();

    Set<CqlBuilderLookup.Lookup> definitions =
        new TreeSet<CqlBuilderLookup.Lookup>(
            new CqlBuilderLookupComparator<CqlBuilderLookup.Lookup>());

    definitions.add(lookup);
    assertEquals(definitions.size(), 1);
  }

  @Test
  void testAddTwoIdentical() {

    CqlBuilderLookup.Lookup lookup =
        CqlBuilderLookup.Lookup.builder()
            .name("SDE Race")
            .libraryAlias("SDE")
            .libraryName("Supplemental Data")
            .startLine(11)
            .build();

    CqlBuilderLookup.Lookup lookup2 =
        CqlBuilderLookup.Lookup.builder()
            .name("SDE Race")
            .libraryAlias("SDE")
            .libraryName("Supplemental Data")
            .startLine(11)
            .build();

    Set<CqlBuilderLookup.Lookup> definitions =
        new TreeSet<CqlBuilderLookup.Lookup>(
            new CqlBuilderLookupComparator<CqlBuilderLookup.Lookup>());

    definitions.add(lookup);
    definitions.add(lookup2);
    assertEquals(definitions.size(), 1);
  }

  @Test
  void testSameNameDifferentAlias() {

    CqlBuilderLookup.Lookup lookup =
        CqlBuilderLookup.Lookup.builder()
            .name("SDE Race")
            .libraryAlias("SDE")
            .libraryName("Supplemental Data")
            .startLine(11)
            .build();

    CqlBuilderLookup.Lookup lookup2 =
        CqlBuilderLookup.Lookup.builder()
            .name("SDE Race")
            .libraryName("Supplemental Data")
            .startLine(11)
            .build();

    Set<CqlBuilderLookup.Lookup> definitions =
        new TreeSet<CqlBuilderLookup.Lookup>(
            new CqlBuilderLookupComparator<CqlBuilderLookup.Lookup>());

    definitions.add(lookup);
    definitions.add(lookup2);
    assertEquals(2, definitions.size());
  }
}
