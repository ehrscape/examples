package com.marand.thinkmed.medications.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentContent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Miha Anzicek
 * @author Mitja Lapajne
 */
public class ExternalPrescriptionPackageDto extends DataTransferObject implements TherapyDocumentContent
{
  private String prescriptionPackageId;
  private DateTime prescriptionDate;
  private final List<ExternalPrescriptionTherapyDto> externalPrescriptionTherapies = new ArrayList<>();

  @Override
  public String getContentId()
  {
    return prescriptionPackageId;
  }

  public String getPrescriptionPackageId()
  {
    return prescriptionPackageId;
  }

  public void setPrescriptionPackageId(final String prescriptionPackageId)
  {
    this.prescriptionPackageId = prescriptionPackageId;
  }

  public DateTime getPrescriptionDate()
  {
    return prescriptionDate;
  }

  public void setPrescriptionDate(final DateTime prescriptionDate)
  {
    this.prescriptionDate = prescriptionDate;
  }

  public void addExternalPrescriptionTherapy(final ExternalPrescriptionTherapyDto externalPrescriptionTherapyDto)
  {
    externalPrescriptionTherapies.add(externalPrescriptionTherapyDto);
  }

  public List<ExternalPrescriptionTherapyDto> getExternalPrescriptionTherapies()
  {
    return externalPrescriptionTherapies;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("prescriptionDate", prescriptionDate)
        .append("prescriptionPackageId", prescriptionPackageId)
        .append("externalPrescriptionTherapies", externalPrescriptionTherapies)
    ;
  }
}
