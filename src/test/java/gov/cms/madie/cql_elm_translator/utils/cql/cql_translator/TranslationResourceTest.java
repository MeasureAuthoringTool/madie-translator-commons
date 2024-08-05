package gov.cms.madie.cql_elm_translator.utils.cql.cql_translator;

import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Disabled;

class TranslationResourceTest {
  TranslationResource translationResource = TranslationResource.getInstance(true);

  @Disabled
  //  @Test
  void buildTranslator_checkExceptionHandling() {

    Assertions.assertThrows(
        TranslationFailureException.class,
        () -> {
          translationResource.buildTranslator(null, null, null);
        });
  }
}
