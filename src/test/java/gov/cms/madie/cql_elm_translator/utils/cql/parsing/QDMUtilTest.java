package gov.cms.madie.cql_elm_translator.utils.cql.parsing;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;

@ExtendWith(MockitoExtension.class)
class QDMUtilTest {

  //   How are we populating this dataTypes to Attributes Map
  //  @Test
  @Disabled
  void getQDMContainer() {
    QDMContainer qdmContainer = QDMUtil.getQDMContainer();
    assertEquals(84, qdmContainer.getDatatypes().size());
    assertEquals(71, qdmContainer.getAttributes().size());
  }
}
