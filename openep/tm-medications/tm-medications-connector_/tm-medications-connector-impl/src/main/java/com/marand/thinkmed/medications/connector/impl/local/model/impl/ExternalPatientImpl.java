package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatient;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatientAllergy;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_patient")
public class ExternalPatientImpl extends ExternalEntityImpl implements ExternalPatient
{
  private String identNumber;
  private String identNumberType;
  private String name;
  private DateTime birthDate;
  private Double weightInKg;
  private Double heightInCm;
  private Gender gender;
  private Set<ExternalPatientAllergy> patientAllergies;
  private String patientImagePath;
  private String address;

  @Override
  public String getIdentNumber()
  {
    return identNumber;
  }

  @Override
  public void setIdentNumber(final String identNumber)
  {
    this.identNumber = identNumber;
  }

  @Override
  public String getIdentNumberType()
  {
    return identNumberType;
  }

  @Override
  public void setIdentNumberType(final String identNumberType)
  {
    this.identNumberType = identNumberType;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public void setName(final String name)
  {
    this.name = name;
  }

  @Override
  @Type(type = "com.marand.maf.core.hibernate.type.DateTimeType")
  public DateTime getBirthDate()
  {
    return birthDate;
  }

  @Override
  public void setBirthDate(final DateTime birthDate)
  {
    this.birthDate = birthDate;
  }

  @Override
  public Double getWeightInKg()
  {
    return weightInKg;
  }

  @Override
  public void setWeightInKg(final Double weightInKg)
  {
    this.weightInKg = weightInKg;
  }

  @Override
  public Double getHeightInCm()
  {
    return heightInCm;
  }

  @Override
  public void setHeightInCm(final Double heightInCm)
  {
    this.heightInCm = heightInCm;
  }

  @Override
  @Enumerated(EnumType.STRING)
  public Gender getGender()
  {
    return gender;
  }

  @Override
  public void setGender(final Gender gender)
  {
    this.gender = gender;
  }

  @Override
  @OneToMany(targetEntity = ExternalPatientAllergyImpl.class, mappedBy = "allergy", fetch = FetchType.LAZY)
  public Set<ExternalPatientAllergy> getPatientAllergies()
  {
    return patientAllergies;
  }

  @Override
  public void setPatientAllergies(final Set<ExternalPatientAllergy> patientAllergies)
  {
    this.patientAllergies = patientAllergies;
  }

  @Override
  public String getPatientImagePath()
  {
    return patientImagePath;
  }

  @Override
  public void setPatientImagePath(final String patientImagePath)
  {
    this.patientImagePath = patientImagePath;
  }

  @Override
  public String getAddress()
  {
    return address;
  }

  @Override
  public void setAddress(final String address)
  {
    this.address = address;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("identNumber", identNumber)
        .append("identNumberType", identNumberType)
        .append("name", name)
        .append("birthDate", birthDate)
        .append("weightInKg", weightInKg)
        .append("heightInCm", heightInCm)
        .append("gender", gender)
        .append("patientAllergies", patientAllergies)
        .append("patientImagePath", patientImagePath)
        .append("address", address)
        ;
  }
}
