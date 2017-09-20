package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.maf.core.hibernate.entity.AbstractEffectiveEntity;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalAllergy;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatient;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatientAllergy;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_patient_allergy")
public class ExternalPatientAllergyImpl extends AbstractEffectiveEntity implements ExternalPatientAllergy
{
  private ExternalPatient patient;
  private ExternalAllergy allergy;

  @Override
  @ManyToOne(targetEntity = ExternalPatientImpl.class)
  public ExternalPatient getPatient()
  {
    return patient;
  }

  @Override
  public void setPatient(final ExternalPatient patient)
  {
    this.patient = patient;
  }

  @Override
  @ManyToOne(targetEntity = ExternalAllergyImpl.class)
  public ExternalAllergy getAllergy()
  {
    return allergy;
  }

  @Override
  public void setAllergy(final ExternalAllergy allergy)
  {
    this.allergy = allergy;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("patient", patient)
        .append("allergy", allergy)
        ;
  }
}
