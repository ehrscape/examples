package com.marand.thinkmed.medications.model;

import com.marand.maf.core.data.entity.PermanentEntity;

/**
 * @author Nejc Korasa
 */
public interface MentalHealthTemplateMember extends PermanentEntity
{
  Medication getMedication();

  void setMedication(final Medication medication);

  MentalHealthTemplate getMentalHealthTemplate();

  void setMentalHealthTemplate(final MentalHealthTemplate medicationGroupTemplate);
}
