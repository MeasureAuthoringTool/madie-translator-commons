package gov.cms.madie.cql_elm_translator.utils.cql.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gov.cms.madie.cql_elm_translator.utils.cql.parsing.model.CQLCode;
import gov.cms.madie.cql_elm_translator.utils.cql.parsing.model.CQLValueSet;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// TODO Move to madie java models
public class DataCriteria {
  @Builder.Default private Map<CQLCode, Set<String>> dataCriteriaWithCodes = new HashMap<>();

  @Builder.Default
  private Map<CQLValueSet, Set<String>> dataCriteriaWithValueSets = new HashMap<>();
}
