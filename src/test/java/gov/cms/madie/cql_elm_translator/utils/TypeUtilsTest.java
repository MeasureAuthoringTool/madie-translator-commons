package gov.cms.madie.cql_elm_translator.utils;

import gov.cms.madie.cql_elm_translator.StaticUtil;
import org.cqframework.cql.cql2elm.tracking.Trackable;
import org.hl7.cql.model.DataType;
import org.hl7.cql.model.SimpleType;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.ExpressionDef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeUtilsTest {

  @Test
  void getResultTypeStr() throws Exception {
    // given
    Trackable mock = mock(Trackable.class);
    StaticUtil.setPublicStaticFinalField(Trackable.class, "INSTANCE", mock);

    DataType dataType = new SimpleType("boolean");
    lenient().when(mock.getResultType(any(Element.class))).thenReturn(dataType);

    // when
    String resultTypeStr = TypeUtils.getResultTypeStr(new ExpressionDef());

    // then
    assertThat(resultTypeStr, is(equalTo("boolean")));
  }
}
