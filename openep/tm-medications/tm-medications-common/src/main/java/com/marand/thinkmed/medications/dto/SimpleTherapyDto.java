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

package com.marand.thinkmed.medications.dto;

import java.util.Collections;
import java.util.List;

import com.marand.thinkmed.medications.MedicationOrderFormType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public abstract class SimpleTherapyDto extends TherapyDto
{
  private MedicationDto medication;
  private String quantityUnit;
  private DoseFormDto doseForm;
  private String quantityDenominatorUnit;

  private String quantityDisplay;

  protected SimpleTherapyDto(final boolean variable)
  {
    super(MedicationOrderFormType.SIMPLE, variable);
  }

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public String getQuantityUnit()
  {
    return quantityUnit;
  }

  public void setQuantityUnit(final String quantityUnit)
  {
    this.quantityUnit = quantityUnit;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
  }

  public String getQuantityDenominatorUnit()
  {
    return quantityDenominatorUnit;
  }

  public void setQuantityDenominatorUnit(final String quantityDenominatorUnit)
  {
    this.quantityDenominatorUnit = quantityDenominatorUnit;
  }

  public String getQuantityDisplay()
  {
    return quantityDisplay;
  }

  public void setQuantityDisplay(final String quantityDisplay)
  {
    this.quantityDisplay = quantityDisplay;
  }

  @Override
  public boolean isNormalInfusion()
  {
    return false;
  }

  @Override
  public List<MedicationDto> getMedications()
  {
    return Collections.singletonList(medication);
  }

  @Override
  public Long getMainMedicationId()
  {
    return medication == null ? null : medication.getId();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("medication", medication)
        .append("quantityUnit", quantityUnit)
        .append("doseForm", doseForm)
        .append("quantityDenominatorUnit", quantityDenominatorUnit)
        .append("quantityDisplay", quantityDisplay);
  }
}
