package gov.cms.madie.cql_elm_translator.utils.cql.parsing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: Auto-generated Javadoc
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CQLCodeSystem {

  /** The id. */
  private String id;

  /** The code system. */
  private String codeSystem;

  /** The code system name. */
  private String codeSystemName;

  /** The code system version. */
  private String codeSystemVersion;

  /** The code system OID. */
  private String OID;

  /**
   * stores off the version uri. example: codesystem "SNOMEDCT:2017-09":
   * 'http://snomed.info/sct/731000124108' version
   * 'http://snomed.info/sct/731000124108/version/201709'
   */
  private String versionUri;
}
