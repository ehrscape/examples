package com.marand.thinkmed.medications.connector.impl.provider.fhir;

import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.exception.SystemException;
import com.marand.maf.core.server.util.DefinedLocaleHolder;

/**
 * @author Mitja Lapajne
 */
public class FhirValidationException extends SystemException
{
  public FhirValidationException(final String dictionaryKey, final Object... params)
  {
    super(getFhirValidationMessage(dictionaryKey, params));
  }

  private static String getFhirValidationMessage(final String dictionaryKey, final Object... params)
  {
    final Locale locale = DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale();
    return Dictionary.getEntry("fhir.validation.failed", locale) + " - " +
        Dictionary.getMessageWithLocale(dictionaryKey, locale, params);
  }
}
