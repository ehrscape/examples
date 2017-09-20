/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.marand.thinkmed.medications.model;

import com.marand.maf.core.data.entity.TemporalEntity;
import com.marand.thinkmed.medications.TitrationType;

/**
 * @author Klavdij Lapajne
 */
public interface MedicationVersion extends TemporalEntity
{
  String getName();

  void setName(String name);

  String getShortName();

  void setShortName(String shortName);

  String getLongName();

  void setLongName(String longName);

  MedicationGeneric getMedicationGeneric();

  void setMedicationGeneric(MedicationGeneric medicationGeneric);

  DoseForm getDoseForm();

  void setDoseForm(DoseForm doseForm);

  AtcClassification getAtcClassification();

  void setAtcClassification(AtcClassification atcClassification);

  MedicationBasicUnit getBasicUnit();

  void setBasicUnit(MedicationBasicUnit basicUnit);

  Medication getMedication();

  void setMedication(final Medication medication);

  Boolean getActive();

  void setActive(final Boolean active);

  Boolean getSuggestSwitchToOral();

  void setSuggestSwitchToOral(Boolean promptSwitchToOral);

  Boolean getReviewReminder();

  void setReviewReminder(Boolean reviewReminder);

  Boolean getControlledDrug();

  void setControlledDrug(Boolean controlledDrug);

  Boolean isMentalHealthDrug();

  void setMentalHealthDrug(final Boolean mentalHealthDrug);

  String getMedicationPackaging();

  void setMedicationPackaging(final String medicationPackaging);

  Boolean getClinicalTrialMedication();

  void setClinicalTrialMedication(final Boolean clinicalTrialMedication);

  Boolean getUnlicensedMedication();

  void setUnlicensedMedication(final Boolean unlicensedMedication);

  Boolean getHighAlertMedication();

  void setHighAlertMedication(final Boolean highAlertMedication);

  Boolean getBlackTriangleMedication();

  void setBlackTriangleMedication(final Boolean blackTriangleMedication);

  Boolean getNotForPrn();

  void setNotForPrn(final Boolean notForPrn);

  Boolean getInpatientMedication();

  void setInpatientMedication(final Boolean inpatientMedication);

  Boolean getOutpatientMedication();

  void setOutpatientMedication(final Boolean outpatientMedication);

  Double getRoundingFactor();

  void setRoundingFactor(Double roundingFactor);

  TitrationType getTitration();

  void setTitration(TitrationType titration);

  String getPrice();

  void setPrice(String price);

  Boolean isExpensiveDrug();

  void setExpensiveDrug(Boolean expensiveDrug);
}
