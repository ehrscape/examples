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

package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class StartAdministrationDto extends AdministrationDto
    implements DoseAdministration, PlannedDoseAdministration, InfusionBagAdministration
{
  private StartAdministrationSubtype startAdministrationSubtype;
  private MedicationDto substituteMedication;
  private TherapyDoseDto administeredDose;
  private TherapyDoseDto plannedDose;
  private boolean differentFromOrder;
  private Double duration; // in minutes
  private InfusionBagDto infusionBag;

  public StartAdministrationDto()
  {
    super(AdministrationTypeEnum.START);
  }

  protected StartAdministrationDto(final StartAdministrationSubtype startAdministrationSubtype)
  {
    super(AdministrationTypeEnum.START);
    this.startAdministrationSubtype = startAdministrationSubtype;
  }

  public StartAdministrationSubtype getStartAdministrationSubtype()
  {
    return startAdministrationSubtype;
  }

  public MedicationDto getSubstituteMedication()
  {
    return substituteMedication;
  }

  public void setSubstituteMedication(final MedicationDto substituteMedication)
  {
    this.substituteMedication = substituteMedication;
  }

  @Override
  public TherapyDoseDto getAdministeredDose()
  {
    return administeredDose;
  }

  @Override
  public void setAdministeredDose(final TherapyDoseDto administeredDose)
  {
    this.administeredDose = administeredDose;
  }

  @Override
  public TherapyDoseDto getPlannedDose()
  {
    return plannedDose;
  }

  @Override
  public void setPlannedDose(final TherapyDoseDto plannedDose)
  {
    this.plannedDose = plannedDose;
  }

  public boolean isDifferentFromOrder()
  {
    return differentFromOrder;
  }

  public void setDifferentFromOrder(final boolean differentFromOrder)
  {
    this.differentFromOrder = differentFromOrder;
  }

  @Override
  public InfusionBagDto getInfusionBag()
  {
    return infusionBag;
  }

  @Override
  public void setInfusionBag(final InfusionBagDto infusionBag)
  {
    this.infusionBag = infusionBag;
  }

  public Double getDuration()
  {
    return duration;
  }

  public void setDuration(final Double duration)
  {
    this.duration = duration;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("startAdministrationSubtype", startAdministrationSubtype)
        .append("substituteMedication", substituteMedication)
        .append("administeredDose", administeredDose)
        .append("plannedDose", plannedDose)
        .append("differentFromOrder", differentFromOrder)
        .append("duration", duration)
        .append("infusionBag", infusionBag)
    ;
  }
}