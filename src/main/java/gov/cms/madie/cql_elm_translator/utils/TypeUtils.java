package gov.cms.madie.cql_elm_translator.utils;

import org.cqframework.cql.cql2elm.tracking.Trackable;
import org.hl7.cql.model.DataType;
import org.hl7.elm.r1.Element;

public class TypeUtils {

  public static String getResultTypeStr(Element element) {
    DataType resultType = Trackable.INSTANCE.getResultType(element);
    return resultType == null ? null : resultType.toString();
  }
}
