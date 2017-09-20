package com.marand.thinkmed.medications.connector.impl.local.model.impl;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCaseDisease;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCase;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalDiseaseType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
@Entity
@Table(name = "ext_central_case_disease")
public class ExternalCentralCaseDiseaseImpl extends AbstractPermanentEntity implements ExternalCentralCaseDisease
{
  private ExternalDiseaseType disease;
  private ExternalCentralCase centralCase;

  @Override
  @ManyToOne(targetEntity = ExternalDiseaseTypeImpl.class)
  public ExternalDiseaseType getDisease()
  {
    return disease;
  }

  @Override
  public void setDisease(final ExternalDiseaseType disease)
  {
    this.disease = disease;
  }

  @Override
  @ManyToOne(targetEntity = ExternalCentralCaseImpl.class)
  public ExternalCentralCase getCentralCase()
  {
    return centralCase;
  }

  @Override
  public void setCentralCase(final ExternalCentralCase centralCase)
  {
    this.centralCase = centralCase;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("disease", disease)
        .append("centralCase", centralCase)
        ;
  }
}
