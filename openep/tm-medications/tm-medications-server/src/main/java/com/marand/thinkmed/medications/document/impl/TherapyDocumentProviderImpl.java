package com.marand.thinkmed.medications.document.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.document.TherapyDocumentProvider;
import com.marand.thinkmed.medications.document.TherapyDocumentProviderPlugin;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentType;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Mitja Lapajne
 */
public class TherapyDocumentProviderImpl implements TherapyDocumentProvider
{
  private List<TherapyDocumentProviderPlugin> providerPlugins;

  @Required
  public void setProviderPlugins(final List<TherapyDocumentProviderPlugin> providerPlugins)
  {
    this.providerPlugins = providerPlugins;
  }

  @Override
  public TherapyDocumentsDto getTherapyDocuments(
      final String patientId,
      final Integer numberOfResults,
      final Integer resultsOffset,
      final DateTime when,
      final Locale locale)
  {
    final List<TherapyDocumentDto> documentsList = new ArrayList<>();
    for (final TherapyDocumentProviderPlugin plugin : providerPlugins)
    {
      final int numberOfResultsPerPlugin = numberOfResults + resultsOffset + 1;
      final List<TherapyDocumentDto> pluginDocumentsList =
          plugin.getTherapyDocuments(patientId, numberOfResultsPerPlugin, when, locale);
      documentsList.addAll(pluginDocumentsList);
    }

    Collections.sort(documentsList, (d1, d2) -> d2.getCreateTimestamp().compareTo(d1.getCreateTimestamp()));

    final TherapyDocumentsDto therapyDocumentsDto = new TherapyDocumentsDto();
    final boolean moreRecordsExist = documentsList.size() > resultsOffset + numberOfResults;
    final int searchToIndex = moreRecordsExist ? resultsOffset + numberOfResults : documentsList.size();
    therapyDocumentsDto.setDocuments(new ArrayList<>(documentsList.subList(resultsOffset, searchToIndex)));
    therapyDocumentsDto.setMoreRecordsExist(moreRecordsExist);
    return therapyDocumentsDto;
  }

  @Override
  public TherapyDocumentDto getTherapyDocument(
      final String patientId,
      final String contentId,
      final TherapyDocumentType documentType,
      final DateTime when,
      final Locale locale)
  {
    for (final TherapyDocumentProviderPlugin plugin : providerPlugins)
    {
      if (plugin.getPluginDocumentTypes().contains(documentType))
      {
        return plugin.getTherapyDocument(patientId, contentId, when, locale);
      }
    }
    return null;
  }
}
