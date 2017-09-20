package com.marand.thinkmed.medications.dto.mentalHealth;

import java.util.ArrayList;
import java.util.Collection;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentContent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class MentalHealthDocumentDto extends DataTransferObject implements JsonSerializable, TherapyDocumentContent
{
  private final String compositionUId;
  private final DateTime createdTime;
  private final NamedExternalDto creator;
  private final NamedExternalDto careProvider;

  private final String patientId;
  private final MentalHealthDocumentType mentalHealthDocumentType;
  private final Integer bnfMaximum;
  private Collection<MentalHealthMedicationDto> mentalHealthMedicationDtoList = new ArrayList<>();
  private Collection<MentalHealthTemplateDto> mentalHealthTemplateDtoList = new ArrayList<>();

  public MentalHealthDocumentDto(
      final String compositionUId,
      final DateTime createdTime,
      final NamedExternalDto creator,
      final String patientId,
      final NamedExternalDto careProvider,
      final MentalHealthDocumentType mentalHealthDocumentType,
      final Integer bnfMaximum,
      final Collection<MentalHealthMedicationDto> mentalHealthMedicationDtoList,
      final Collection<MentalHealthTemplateDto> mentalHealthTemplateDtoList)
  {
    this.compositionUId = compositionUId;
    this.createdTime = createdTime;
    this.creator = creator;
    this.patientId = patientId;
    this.careProvider = careProvider;
    this.mentalHealthDocumentType = mentalHealthDocumentType;
    this.bnfMaximum = bnfMaximum;
    this.mentalHealthMedicationDtoList = mentalHealthMedicationDtoList;
    this.mentalHealthTemplateDtoList = mentalHealthTemplateDtoList;
  }

  public String getCompositionUId()
  {
    return compositionUId;
  }

  public DateTime getCreatedTime()
  {
    return createdTime;
  }

  public NamedExternalDto getCreator()
  {
    return creator;
  }

  public MentalHealthDocumentType getMentalHealthDocumentType()
  {
    return mentalHealthDocumentType;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public NamedExternalDto getCareProvider()
  {
    return careProvider;
  }

  public Integer getBnfMaximum()
  {
    return bnfMaximum;
  }

  public Collection<MentalHealthMedicationDto> getMentalHealthMedicationDtoList()
  {
    return mentalHealthMedicationDtoList;
  }

  public Collection<MentalHealthTemplateDto> getMentalHealthTemplateDtoList()
  {
    return mentalHealthTemplateDtoList;
  }

  @Override
  public String getContentId()
  {
    return compositionUId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUId", compositionUId)
        .append("patientId", patientId)
        .append("creator", creator)
        .append("createdTime", createdTime)
        .append("careProvider", careProvider)
        .append("mentalHealthDocumentType", mentalHealthDocumentType)
        .append("bnfMaximum", bnfMaximum)
        .append("mentalHealthMedicationDtoList", mentalHealthMedicationDtoList)
        .append("mentalHealthTemplateDtoList", mentalHealthTemplateDtoList)
    ;
  }
}
