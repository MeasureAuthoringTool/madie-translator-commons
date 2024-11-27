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

    //    if (StringUtils.equals(lookup1.getName(), "SDE Race")
    //        || StringUtils.equals(lookup2.getName(), "SDE Race")) {
    //      log.info(
    //          "LOOKUP1:Alias = {}, Name = {}, Linenumber = {};  ",
    //          lookup1.getLibraryAlias(),
    //          lookup1.getName(),
    //          lookup1.getStartLine());
    //      log.info(
    //          "LOOKUP2:Alias = {}, Name = {}, Linenumber = {};  ",
    //          lookup2.getLibraryAlias(),
    //          lookup2.getName(),
    //          lookup2.getStartLine());
    //    }
    // if the names are the same
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
    if (lookup1.getName().equals("SDE Race")) {
      log.info("Adding {} according to comparater {}", lookup1.getName(), lookup1.getStartLine());
    }
    return result;
  }
}
