package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.marand.maf.core.hibernate.entity.AbstractPermanentEntity;
import com.marand.thinkmed.medications.model.Medication;
import com.marand.thinkmed.medications.model.MentalHealthTemplate;
import com.marand.thinkmed.medications.model.MentalHealthTemplateMember;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Nejc Korasa
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedication", columnList = "medication_id"),
    @Index(name = "xfMedicationTemplate", columnList = "mental_health_template_id")})
public class MentalHealthTemplateMemberImpl extends AbstractPermanentEntity implements MentalHealthTemplateMember
{
  private Medication medication;
  private MentalHealthTemplate mentalHealthTemplate;

  @Override
  @ManyToOne(targetEntity = MedicationImpl.class, optional = false, fetch = FetchType.LAZY)
  public Medication getMedication()
  {
    return medication;
  }

  @Override
  public void setMedication(final Medication medication)
  {
    this.medication = medication;
  }

  @Override
  @ManyToOne(targetEntity = MentalHealthTemplateImpl.class, optional = false, fetch = FetchType.LAZY)
  public MentalHealthTemplate getMentalHealthTemplate()
  {
    return mentalHealthTemplate;
  }

  @Override
  public void setMentalHealthTemplate(final MentalHealthTemplate medicationGroupTemplate)
  {
    this.mentalHealthTemplate = medicationGroupTemplate;
  }
}
