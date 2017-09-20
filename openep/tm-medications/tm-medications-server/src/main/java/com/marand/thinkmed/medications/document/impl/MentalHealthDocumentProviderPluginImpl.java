package com.marand.thinkmed.medications.document.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.medications.document.TherapyDocumentProviderPlugin;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentType;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentTypeEnum;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.mentalhealth.MentalHealthFormProvider;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class MentalHealthDocumentProviderPluginImpl implements TherapyDocumentProviderPlugin
{
  private MentalHealthFormProvider mentalHealthFormProvider;

  @Required
  public void setMentalHealthFormProvider(final MentalHealthFormProvider mentalHealthFormProvider)
  {
    this.mentalHealthFormProvider = mentalHealthFormProvider;
  }

  @Override
  public List<TherapyDocumentDto> getTherapyDocuments(
      final String patientId,
      final Integer numberOfResults,
      final DateTime when,
      final Locale locale)
  {
    return mentalHealthFormProvider.getMentalHealthDocuments(patientId, Intervals.infiniteTo(when), numberOfResults)
        .stream()
        .map(mapToTherapyDocument())
        .collect(Collectors.toList());
  }

  private Function<MentalHealthDocumentDto, TherapyDocumentDto> mapToTherapyDocument()
  {
    return mentalHealthDocument ->
    {
      final TherapyDocumentDto therapyDocumentDto = new TherapyDocumentDto();
      therapyDocumentDto.setContent(mentalHealthDocument);
      therapyDocumentDto.setCreateTimestamp(mentalHealthDocument.getCreatedTime());
      therapyDocumentDto.setDocumentType(getTherapyDocumentType(mentalHealthDocument));
      therapyDocumentDto.setCreator(mentalHealthDocument.getCreator());
      therapyDocumentDto.setCareProvider(mentalHealthDocument.getCareProvider());
      return therapyDocumentDto;
    };
  }

  private TherapyDocumentTypeEnum getTherapyDocumentType(final MentalHealthDocumentDto mentalHealthDocument)
  {
    if (mentalHealthDocument.getMentalHealthDocumentType() == MentalHealthDocumentType.T2)
    {
      return TherapyDocumentTypeEnum.T2;
    }
    else if(mentalHealthDocument.getMentalHealthDocumentType() == MentalHealthDocumentType.T3)
    {
      return TherapyDocumentTypeEnum.T3;
    }
    else
    {
      throw new UnsupportedOperationException(
          "Mental health document type " + mentalHealthDocument.getMentalHealthDocumentType() + " is not yet supported!");
    }
  }

  @Override
  public TherapyDocumentDto getTherapyDocument(
      final String patientId,
      final String contentId,
      final DateTime when,
      final Locale locale)
  {
    return mapToTherapyDocument().apply(mentalHealthFormProvider.getMentalHealthDocument(patientId, contentId));
  }

  @Override
  public Collection<TherapyDocumentType> getPluginDocumentTypes()
  {
    final List<TherapyDocumentType> types = new ArrayList<>();
    types.add(TherapyDocumentTypeEnum.T2);
    types.add(TherapyDocumentTypeEnum.T3);
    return types;
  }
}
