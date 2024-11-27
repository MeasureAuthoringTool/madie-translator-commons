package gov.cms.madie.cql_elm_translator.dto;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CqlBuilderLookupComparator<T> implements Comparator<CqlBuilderLookup.Lookup> {

  /***
   * This is required because the name/line numbers can conflict between line
   * numbers
   */
  public int compare(CqlBuilderLookup.Lookup lookup1, CqlBuilderLookup.Lookup lookup2) {

    int result;

    if (StringUtils.equalsIgnoreCase(lookup1.getName(), lookup2.getName())
        && StringUtils.equalsIgnoreCase(lookup1.getLibraryAlias(), lookup2.getLibraryAlias())) {
      result = 0;
    } else if (lookup1.getStartLine() == lookup2.getStartLine()) {
      // if the names are different but the lines are the same
      result = lookup1.getStartLine() - lookup2.getStartLine() + 1;
    } else {
      result = lookup1.getStartLine() - lookup2.getStartLine();
    }

    return result;
  }
}
