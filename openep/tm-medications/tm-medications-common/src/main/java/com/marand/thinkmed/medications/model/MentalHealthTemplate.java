package com.marand.thinkmed.medications.model;

import java.util.List;

import com.marand.maf.core.data.entity.CatalogEntity;

/**
 * @author Nejc Korasa
 */
public interface MentalHealthTemplate extends CatalogEntity
{
  MedicationRoute getMedicationRoute();

  void setMedicationRoute(MedicationRoute medicationRoute);

  List<MentalHealthTemplateMember> getMentalHealthTemplateMemberList();

  void setMentalHealthTemplateMemberList(List<MentalHealthTemplateMember> mentalHealthTemplateMemberList);
}