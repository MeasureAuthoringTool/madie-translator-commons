package gov.cms.madie.cql_elm_translator.utils.cql.data;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import kotlinx.io.Source;
import org.apache.commons.lang3.StringUtils;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.cqframework.cql.cql2elm.utils.SourceKt;
import org.hl7.elm.r1.VersionedIdentifier;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
public class RequestData {

  @Getter private String cqlData;

  @Getter private CqlCompilerException.ErrorSeverity errorSeverity;
  @Getter private VersionedIdentifier sourceInfo;

  @Getter String identifier;

  LibraryBuilder.SignatureLevel signatures;
  Boolean annotations;
  Boolean locators;
  Boolean disableListDemotion;
  Boolean disableListPromotion;
  Boolean disableMethodInvocation;
  Boolean validateUnits;
  Boolean resultTypes;

  public Source getCqlAsSource() {
    return StringUtils.isBlank(cqlData) ? null : SourceKt.asSource(cqlData);
  }

  public MultivaluedMap<String, String> createMap() {

    MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
    map.add("annotations", annotations.toString());
    map.add("locators", locators.toString());
    map.add("disable-list-demotion", disableListDemotion.toString());
    map.add("disable-list-promotion", disableListPromotion.toString());
    map.add("disable-method-invocation", disableMethodInvocation.toString());
    map.add("validate-units", validateUnits.toString());
    map.add("error-severity", errorSeverity.toString());
    map.add("result-types", resultTypes.toString());

    // Enforcing detailed errors and not providing an option to Client
    map.add("detailed-errors", Boolean.TRUE.toString());

    if (signatures != null) {
      map.add("signatures", signatures.name());
    }

    return map;
  }

  public static class RequestDataBuilder {

    public RequestDataBuilder identifier(String identifier) {

      this.sourceInfo = new VersionedIdentifier().withId(identifier).withSystem("text/cql");
      return this;
    }
  }
}
