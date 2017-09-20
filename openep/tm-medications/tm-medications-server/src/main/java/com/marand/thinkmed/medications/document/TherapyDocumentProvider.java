package com.marand.thinkmed.medications.document;

import java.util.Locale;

import com.marand.thinkmed.medications.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentType;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import org.joda.time.DateTime;

/**
* @author Mitja Lapajne
    */
public interface TherapyDocumentProvider
{
  TherapyDocumentsDto getTherapyDocuments(
      String patientId,
      Integer numberOfResults,
      Integer resultsOffset,
      DateTime when,
      Locale locale);

  TherapyDocumentDto getTherapyDocument(
      String patientId,
      String contentId,
      TherapyDocumentType documentType,
      DateTime when,
      Locale locale);
}
