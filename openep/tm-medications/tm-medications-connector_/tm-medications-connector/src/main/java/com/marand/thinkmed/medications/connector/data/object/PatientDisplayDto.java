package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class PatientDisplayDto extends NamedExternalDto
{
  private DateTime birthDate;
  private Gender gender;
  private String patientImagePath;

  public PatientDisplayDto(
      final String id,
      final String name,
      final DateTime birthDate,
      final Gender gender,
      final String patientImagePath)
  {
    super(id, name);
    this.birthDate = birthDate;
    this.gender = gender;
    this.patientImagePath = patientImagePath;
  }

  public DateTime getBirthDate()
  {
    return birthDate;
  }

  public void setBirthDate(final DateTime birthDate)
  {
    this.birthDate = birthDate;
  }

  public Gender getGender()
  {
    return gender;
  }

  public void setGender(final Gender gender)
  {
    this.gender = gender;
  }

  public String getPatientImagePath()
  {
    return patientImagePath;
  }

  public void setPatientImagePath(final String patientImagePath)
  {
    this.patientImagePath = patientImagePath;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("birthDate", birthDate)
        .append("gender", gender)
        .append("patientImagePath", patientImagePath);
  }
}
