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

import com.marand.maf.core.data.object.DataObject;
import com.marand.maf.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class InfusionIngredientDto extends DataObject implements JsonSerializable
{
  private MedicationDto medication;
  private Double quantity;
  private String quantityUnit;
  private Double volume;
  private String volumeUnit;
  private DoseFormDto doseForm;

  private String quantityDisplay;

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public Double getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Double quantity)
  {
    this.quantity = quantity;
  }

  public String getQuantityUnit()
  {
    return quantityUnit;
  }

  public void setQuantityUnit(final String quantityUnit)
  {
    this.quantityUnit = quantityUnit;
  }

  public Double getVolume()
  {
    return volume;
  }

  public void setVolume(final Double volume)
  {
    this.volume = volume;
  }

  public String getVolumeUnit()
  {
    return volumeUnit;
  }

  public void setVolumeUnit(final String volumeUnit)
  {
    this.volumeUnit = volumeUnit;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
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
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("medication", medication)
        .append("quantity", quantity)
        .append("quantityUnit", quantityUnit)
        .append("volume", volume)
        .append("volumeUnit", volumeUnit)
        .append("doseForm", doseForm)
        .append("quantityDisplay", quantityDisplay);
  }
}