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

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.DataObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
public class MedicationDataDto extends DataObject
{
  private MedicationDto medication;
  private MedicationRouteDto defaultRoute;
  private DoseFormDto doseForm;
  private Double doseSplitFactor;
  private String basicUnit;
  private boolean antibiotic;
  private MedicationIngredientDto descriptiveIngredient;
  private List<MedicationIngredientDto> medicationIngredients = new ArrayList<>();
  private List<MedicationRouteDto> routes = new ArrayList<>();
  private List<MedicationDocumentDto> medicationDocuments = new ArrayList<>();

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public MedicationRouteDto getDefaultRoute()
  {
    return defaultRoute;
  }

  public void setDefaultRoute(final MedicationRouteDto defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
  }

  public Double getDoseSplitFactor()
  {
    return doseSplitFactor;
  }

  public void setDoseSplitFactor(final Double doseSplitFactor)
  {
    this.doseSplitFactor = doseSplitFactor;
  }

  public String getBasicUnit()
  {
    return basicUnit;
  }

  public void setBasicUnit(final String basicUnit)
  {
    this.basicUnit = basicUnit;
  }

  public boolean isAntibiotic()
  {
    return antibiotic;
  }

  public void setAntibiotic(final boolean antibiotic)
  {
    this.antibiotic = antibiotic;
  }

  public MedicationIngredientDto getDescriptiveIngredient()
  {
    return descriptiveIngredient;
  }

  public void setDescriptiveIngredient(final MedicationIngredientDto descriptiveIngredient)
  {
    this.descriptiveIngredient = descriptiveIngredient;
  }

  public List<MedicationIngredientDto> getMedicationIngredients()
  {
    return medicationIngredients;
  }

  public void setMedicationIngredients(final List<MedicationIngredientDto> medicationIngredients)
  {
    this.medicationIngredients = medicationIngredients;
  }

  public List<MedicationRouteDto> getRoutes()
  {
    return routes;
  }

  public void setRoutes(final List<MedicationRouteDto> routes)
  {
    this.routes = routes;
  }

  public List<MedicationDocumentDto> getMedicationDocuments()
  {
    return medicationDocuments;
  }

  public void setMedicationDocuments(final List<MedicationDocumentDto> medicationDocuments)
  {
    this.medicationDocuments = medicationDocuments;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("medication", medication)
        .append("defaultRoute", defaultRoute)
        .append("doseForm", doseForm)
        .append("doseSplitFactor", doseSplitFactor)
        .append("basicUnit", basicUnit)
        .append("descriptiveIngredient", descriptiveIngredient)
        .append("medicationIngredients", medicationIngredients)
        .append("routes", routes)
        .append("antibiotic", antibiotic)
        .append("medicationDocuments", medicationDocuments)
    ;
  }
}