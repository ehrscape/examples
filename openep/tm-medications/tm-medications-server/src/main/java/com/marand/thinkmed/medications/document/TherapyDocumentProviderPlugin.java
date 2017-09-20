package com.marand.thinkmed.medications.document;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentType;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyDocumentProviderPlugin
{
  List<TherapyDocumentDto> getTherapyDocuments(String patientId, Integer numberOfResults, DateTime when, Locale locale);

  TherapyDocumentDto getTherapyDocument(
      String patientId,
      String contentId,
      DateTime when,
      Locale locale);

  Collection<TherapyDocumentType> getPluginDocumentTypes();
}
